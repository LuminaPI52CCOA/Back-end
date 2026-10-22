# Pipeline de Telemetria e Centralização de Logs com Fluent Bit, AWS S3, AWS Glue e AWS Athena

**Projeto:** LUMINA Management
**Módulo:** Observabilidade, Auditoria e Pipeline de Dados em Nuvem  
**Autor:** Lucas Aquino, Gabriele Dutra, João Carlos, João Caio, Luiz Souza, Thiago Akihito.
**Data:** Outubro de 2026  


# OBS:
Documento Gerado por IA após a implementação completa do FluentBit para fácil entendimento de todos do grupo.
---

## 1. Enunciado e Objetivos da Entrega

> *"Configure o Fluent Bit para capturar logs dos containers da sua aplicação. Se for o caso, configure também para capturar logs do Nginx e logs do sistema operacional. Carregue todos os logs no bucket bronze tier. Entregue a configuração, a estratégia de armazenamento (estrutura das pastas dos logs) e monte um pipeline de consumo destes logs com o AWS Glue + AWS Athena (crie um pipeline dedicado aos logs). Entrega individual. Suba tudo num arquivo .zip."*

Este documento consolida a arquitetura, as configurações operacionais, os scripts de infraestrutura e o pipeline de processamento e análise de logs para a plataforma **LUMINA**.

---

## 2. Visão Geral da Arquitetura de Dados

O ecossistema implementado adota a arquitetura **Medalhão** (*Bronze, Silver e Gold*) já padronizada no projeto LUMINA, estendendo-a com um pipeline 100% dedicado aos dados de telemetria e auditoria.

```
+-----------------------------------------------------------------------------------+
|                            CAMADA DE GERAÇÃO E INGESTÃO                           |
+-----------------------------------------------------------------------------------+
|  [Spring Boot API]       [Nginx Proxy]         [Containers Docker]   [Host Linux] |
|   /var/log/backend      /var/log/nginx        stdout / stderr JSON   /var/log/auth|
+---------+--------------------+-------------------------+--------------------+-----+
          |                    |                         |                    |
          +--------------------+------------+------------+--------------------+
                                            |
                                            v
                         +-------------------------------------+
                         |      AGENTE FLUENT BIT (v5.1)       |
                         |  - Input Tail com SQLite DB Offset  |
                         |  - Parsers Regex, Docker & JSON     |
                         |  - Enriquecimento com Metadados     |
                         |  - Buffer em Disco (Filesystem)     |
                         |  - S3 Output Plugin (GZIP / JSON)   |
                         +------------------+------------------+
                                            |
                                            v
+-----------------------------------------------------------------------------------+
|                         CAMADA BRONZE (Armazenamento Bruto)                       |
+-----------------------------------------------------------------------------------+
|  Amazon S3: s3://lumina-bronze/logs/                                              |
|    |-- nginx/year/month/day/nginx.access-UUID.json.gz                             |
|    |-- backend/year/month/day/app.backend-UUID.json.gz                            |
|    |-- containers/year/month/day/containers.docker-UUID.json.gz                   |
|    `-- system_auth/year/month/day/system.auth-UUID.json.gz                       |
+-----------------------------------+-----------------------------------------------+
                                    |
                                    | Disparo de Evento S3 / Agendamento
                                    v
+-----------------------------------------------------------------------------------+
|                     CAMADA DE PROCESSAMENTO E TRANSFORMAÇÃO                       |
+-----------------------------------------------------------------------------------+
|  AWS Glue ETL Job (PySpark): lumina-logs-bronze-to-silver                         |
|    1. Descompacta e lê JSONs da camada Bronze                                     |
|    2. Normaliza schema unificado (timestamp, source, level, status, path, ip)    |
|    3. Converte para Apache Parquet colunar de alta compressão (Snappy)            |
|    4. Grava na camada Silver com particionamento temporal                         |
+-----------------------------------+-----------------------------------------------+
                                    |
                                    v
+-----------------------------------------------------------------------------------+
|                         CAMADA SILVER & CONSUMO ANALÍTICO                         |
+-----------------------------------------------------------------------------------+
|  Amazon S3: s3://lumina-silver/logs/source=.../year=.../month=.../day=.../*.parquet|
|                                                                                   |
|  AWS Athena (Glue Data Catalog): Database lumina_logs_db                          |
|  - Consultas analíticas SQL interativas sem provisionamento de servidores         |
|  - Auditoria de erros 4xx/5xx, IPs suspeitos, volumetria e SLAs de containers     |
+-----------------------------------------------------------------------------------+
```

---

## 3. Estratégia de Armazenamento no Amazon S3

A taxonomia de diretórios foi desenhada para garantir particionamento determinístico, otimização de custo de varredura (*scan bytes*) no Athena e separação estrita de responsabilidades:

### 3.1. Camada Bronze (`s3://lumina-bronze/logs/`)
Recebe os dados brutos compactados em tempo real enviados pelo Fluent Bit:

```
s3://lumina-bronze/logs/
├── nginx/
│   └── 2026/10/15/nginx.access-c89b41a2-3f12-4a01-92f1.json.gz
├── backend/
│   └── 2026/10/15/app.backend-91e84df2-8172-4b22-83b3.json.gz
├── containers/
│   └── 2026/10/15/containers.docker-1a4e52f0-7b9c-49a3-bd11.json.gz
└── system_auth/
    └── 2026/10/15/system.auth-3d441aa7-109b-4392-8ec2.json.gz
```

* **Vantagens**: Compressão gzip nativa de até 85% do tráfego; chave de formato com ano/mês/dia (`%Y/%m/%d`) e identificador único `$UUID` impedindo sobrescrita.

### 3.2. Camada Silver (`s3://lumina-silver/logs/`)
Armazena a visão analítica limpa gerada pelo AWS Glue em formato Parquet com particionamento estilo Hive:

```
s3://lumina-silver/logs/
├── source=nginx/
│   └── year=2026/month=10/day=15/part-00000.snappy.parquet
├── source=backend/
│   └── year=2026/month=10/day=15/part-00000.snappy.parquet
├── source=containers/
│   └── year=2026/month=10/day=15/part-00000.snappy.parquet
└── source=system_auth/
    └── year=2026/month=10/day=15/part-00000.snappy.parquet
```

* **Vantagens**: Redução drástica do custo no Athena via *partition pruning* (consultas filtram direto por `source = 'nginx' AND year = '2026'`), além de leitura colunar de altíssima performance.

---

## 4. Configuração Completa do Fluent Bit

A solução conta com suporte aos dois formatos de arquivo do Fluent Bit (o formato clássico `fluent-bit.conf` e o formato modernizado `fluent-bit.yaml` da versão 5.1).

### 4.1. Definição dos Parsers (`parsers.conf`)
Arquivo dedicado ao desacoplamento das regras de parsing:

```ini
[PARSER]
    Name        json
    Format      json
    Time_Key    time
    Time_Format %Y-%m-%dT%H:%M:%S.%L
    Time_Keep   On

[PARSER]
    Name        docker
    Format      json
    Time_Key    time
    Time_Format %Y-%m-%dT%H:%M:%S.%L%z
    Time_Keep   On

[PARSER]
    Name        nginx_combined
    Format      regex
    Regex       ^(?<remote>[^ ]*) (?<host>[^ ]*) (?<user>[^ ]*) \[(?<time>[^\]]*)\] "(?<method>\S+)(?: +(?<path>[^\"]*?)(?: +\S*)?)?" (?<code>[^ ]*) (?<size>[^ ]*)(?: "(?<referer>[^\"]*)" "(?<agent>[^\"]*)")?$
    Time_Key    time
    Time_Format %d/%b/%Y:%H:%M:%S %z
    Types       code:integer size:integer

[PARSER]
    Name        syslog
    Format      regex
    Regex       ^(?<time>[^ ]* {1,2}[^ ]* [^ ]*) (?<host>[^ ]*) (?<ident>[a-zA-Z0-9_\/\.\-]*)(?:\[(?<pid>[0-9]+)\])?(?:[^\:]*\:)? *(?<message>.*)$
    Time_Key    time
    Time_Format %b %d %H:%M:%S

[PARSER]
    Name        spring_boot
    Format      regex
    Regex       ^(?<time>\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}(?:Z|[+-]\d{2}:\d{2}))\s+(?<level>[A-Z]+)\s+(?<pid>\d+)\s+---\s+\[(?<service>[^\]]*)\]\s+\[(?<thread>[^\]]*)\]\s+(?<logger>[^\s]+)\s*:\s*(?<message>.*)$
    Time_Key    time
    Time_Format %Y-%m-%dT%H:%M:%S.%L%z
    Time_Keep   On
```

### 4.2. Configuração do Pipeline (`fluent-bit.conf`)

* **SERVICE**: Flush de 1 segundo, buffer de persistência em disco em `/tmp/fluent-bit/storage`, e servidor HTTP de healthcheck na porta `2020`.
* **INPUTS**: Coleta isolada por tag com offsets gravados em bancos SQLite locais (`.db`), garantindo retomada de leitura sem duplicidade após reinicializações.
* **FILTERS**: Adiciona os metadados de governança (`project=lumina`, `tier=bronze`, `environment=dev`).
* **OUTPUTS**: Envio ao Amazon S3 via plugin oficial utilizando autenticação por credenciais parametrizáveis.

---

## 5. Orquestração Local com Docker Compose

Todos os componentes do ecossistema sobem de forma integrada através do arquivo `docker-compose.yml`:

```yaml
services:
  mysql:      # Banco de dados da aplicação Lumina
  backend:    # API Spring Boot gerando logs em stdout e /var/log/backend/app.log
  nginx:      # Proxy reverso e frontend gerando logs em /var/log/nginx/access.log
  fluent-bit: # Agente coletor enviando logs ao S3 e monitoramento na porta 2020
```

### Passo a passo para execução:

1. **Configurar as credenciais no `.env`**:
   Copie o arquivo de exemplo e insira suas credenciais da AWS (ou da AWS Academy):
   ```bash
   cp .env.example .env
   # Edite o arquivo .env preenchendo AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY e BUCKET_BRONZE
   ```

2. **Compilar a aplicação Spring Boot**:
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```

3. **Subir os containers**:
   ```bash
   docker compose up -d --build
   ```

4. **Validar a saúde e o status dos serviços**:
   ```bash
   # Status dos containers
   docker compose ps

   # Logs do coletor Fluent Bit
   docker compose logs -f fluent-bit

   # Métricas internas e healthcheck do Fluent Bit
   curl http://localhost:2020/api/v2/health
   curl http://localhost:2020/api/v1/metrics
   ```

5. **Gerar tráfego para produzir logs**:
   ```bash
   # Requisições de sucesso e falha proposital no Nginx
   curl http://localhost:8888/health
   curl http://localhost:8888/api/usuarios
   curl http://localhost:8888/admin/config-bloqueado
   ```

6. **Desligar os containers ao finalizar**:
   ```bash
   docker compose down
   ```

---

## 6. Pipeline Dedicado de Consumo (AWS Glue + AWS Athena)

### 6.1. Script Glue ETL (`pipeline-logs/job_logs_bronze_to_silver.py`)
Script PySpark executado sob demanda ou por evento S3 (`ObjectCreated`):
* Lê os arquivos brutos JSON e compactados em GZIP da camada Bronze.
* Infere e normaliza colunas comuns (`event_timestamp`, `source`, `level`, `message`, `http_status`, `http_method`, `http_path`, `client_ip`, `service_name`).
* Salva os dados em formato colunar Parquet particionado por `source/year/month/day/`.

### 6.2. DDL e Queries Analíticas no AWS Athena (`pipeline-logs/athena_logs_queries.sql`)

#### DDL da Tabela Silver Otimizada:
```sql
CREATE EXTERNAL TABLE IF NOT EXISTS lumina_logs_db.logs_silver_parquet (
    event_timestamp TIMESTAMP,
    level STRING,
    message STRING,
    http_status INT,
    http_method STRING,
    http_path STRING,
    client_ip STRING,
    service_name STRING,
    logger_class STRING
)
PARTITIONED BY (
    source STRING,
    year STRING,
    month STRING,
    day STRING
)
STORED AS PARQUET
LOCATION 's3://lumina-silver/logs/'
TBLPROPERTIES ('parquet.compression'='SNAPPY');

MSCK REPAIR TABLE lumina_logs_db.logs_silver_parquet;
```

#### Consultas Analíticas Disponibilizadas:
1. **Detecção de Falhas 4xx/5xx por Endpoint (Nginx)**: Mapeia rotas com maior índice de erros e total de clientes impactados.
2. **Auditoria de Segurança (Top IPs com Falhas)**: Identifica varreduras ou tentativas de acesso indevido.
3. **Distribuição de Severidade por Serviço**: Percentual e contagem de mensagens `ERROR`, `WARN` e `INFO`.
4. **Diagnóstico de Exceções Críticas no Spring Boot**: Filtro instantâneo das últimas stack traces de erro.
5. **Volumetria Diária de Logs**: Acompanhamento do SLA e saúde do pipeline de ingestão.

---

## 7. Evidências e Amostras de Teste

Na pasta `evidencias/` estão disponibilizadas amostras reais produzidas pelo pipeline:
* `sample_nginx_access.json`: Amostra de log de acesso do Nginx estruturado.
* `sample_backend.json`: Amostra de logs da aplicação Spring Boot.
* `sample_container.json`: Amostra de logs de container Docker (stdout/stderr).
* `system.log`: Amostra de logs operacionais do sistema Linux.
* `sample_athena_query_results.md`: Resultados tabulares de simulação das 5 consultas analíticas do Athena.

---

## 8. Conteúdo Empacotado no Arquivo `.zip` de Entrega

```
entrega-fluentbit-logs.zip
├── docker-compose.yml               # Orquestração com Fluent Bit, Nginx, Backend e MySQL
├── Dockerfile                       # Construção da imagem do backend
├── fluent-bit.conf                  # Configuração clássica oficial do Fluent Bit
├── fluent-bit.yaml                  # Configuração YAML v5.1 do Fluent Bit
├── parsers.conf                     # Parsers regex, docker, nginx e spring boot
├── nginx-proxy.conf                 # Configuração do proxy reverso gerador de logs
├── .env.example                     # Modelo documentado de variáveis de ambiente
├── FLUENTBIT_PIPELINE_LOGS.md       # Esta documentação completa com evidências
├── gerar-entrega-zip.ps1            # Script de automação do empacotamento
├── pipeline-logs/
│   ├── job_logs_bronze_to_silver.py # Script PySpark do AWS Glue ETL
│   └── athena_logs_queries.sql      # DDLs e queries analíticas do AWS Athena
└── evidencias/
    ├── sample_nginx_access.json     # Amostra de log Nginx
    ├── sample_backend.json          # Amostra de log Backend
    ├── sample_container.json        # Amostra de log Container
    ├── system.log                   # Amostra de log do Sistema
    └── sample_athena_query_results.md# Evidência das consultas executadas no Athena
```

### Como gerar o arquivo `.zip` automaticamente:
Execute o script PowerShell fornecido na raiz do backend:
```powershell
.\gerar-entrega-zip.ps1
```
O script gera automaticamente o arquivo `entrega-fluentbit-logs.zip` pronto para envio individual.
