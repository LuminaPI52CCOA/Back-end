-- ==============================================================================
-- AWS Athena - DDL e Queries Analíticas para o Pipeline de Logs do Lumina
-- ==============================================================================

-- 1. Criação do Banco de Dados no AWS Glue Data Catalog
CREATE DATABASE IF NOT EXISTS lumina_logs_db
COMMENT 'Banco de dados analitico para telemetria e logs coletados pelo Fluent Bit'
LOCATION 's3://lumina-silver/logs/';

-- ------------------------------------------------------------------------------
-- 2. Tabela Silver: Logs Padronizados em Apache Parquet (Otimizada e Particionada)
-- ------------------------------------------------------------------------------
CREATE EXTERNAL TABLE IF NOT EXISTS lumina_logs_db.logs_silver_parquet (
    event_timestamp TIMESTAMP COMMENT 'Data e hora exata do evento',
    level STRING COMMENT 'Nivel de severidade (INFO, WARN, ERROR)',
    message STRING COMMENT 'Conteudo ou mensagem textual do log',
    http_status INT COMMENT 'Codigo de status HTTP (ex: 200, 404, 500)',
    http_method STRING COMMENT 'Verbo HTTP (GET, POST, PUT, DELETE)',
    http_path STRING COMMENT 'Caminho do endpoint acessado',
    client_ip STRING COMMENT 'Endereco IP de origem',
    service_name STRING COMMENT 'Nome do servico ou aplicacao geradora',
    logger_class STRING COMMENT 'Classe Java ou modulo emissor do log'
)
PARTITIONED BY (
    source STRING COMMENT 'Origem: nginx, backend, containers, system_auth',
    year STRING,
    month STRING,
    day STRING
)
STORED AS PARQUET
LOCATION 's3://lumina-silver/logs/'
TBLPROPERTIES (
    'parquet.compression'='SNAPPY',
    'classification'='parquet'
);

-- Atualizar metadados das particoes na tabela Silver
MSCK REPAIR TABLE lumina_logs_db.logs_silver_parquet;


-- ------------------------------------------------------------------------------
-- 3. Tabela Bronze: Leitura Direta dos Arquivos JSON/GZIP Brutos (Para Auditoria)
-- ------------------------------------------------------------------------------
CREATE EXTERNAL TABLE IF NOT EXISTS lumina_logs_db.logs_bronze_json (
    time STRING,
    log STRING,
    message STRING,
    level STRING,
    code INT,
    method STRING,
    path STRING,
    remote STRING,
    project STRING,
    environment STRING
)
ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES (
    'ignore.malformed.json' = 'true'
)
LOCATION 's3://lumina-bronze/logs/';


-- ==============================================================================
-- 4. QUERIES ANALÍTICAS DE NEGÓCIO E AUDITORIA (AWS ATHENA)
-- ==============================================================================

-- QUERY 1: Detecção de Falhas de Requisição HTTP (Erros 4xx e 5xx por Endpoint)
SELECT 
    http_status,
    http_method,
    http_path,
    COUNT(*) AS total_ocorrencias,
    COUNT(DISTINCT client_ip) AS total_ips_afetados
FROM lumina_logs_db.logs_silver_parquet
WHERE source = 'nginx'
  AND http_status >= 400
GROUP BY http_status, http_method, http_path
ORDER BY total_ocorrencias DESC
LIMIT 20;


-- QUERY 2: Auditoria de Segurança: Top IPs que geraram erros ou tentativas suspeitas
SELECT 
    client_ip,
    COUNT(*) AS total_falhas,
    MIN(event_timestamp) AS primeira_ocorrencia,
    MAX(event_timestamp) AS ultima_ocorrencia,
    ARRAY_AGG(DISTINCT http_path) AS rotas_consultadas
FROM lumina_logs_db.logs_silver_parquet
WHERE http_status IN (401, 403, 404, 500)
GROUP BY client_ip
ORDER BY total_falhas DESC
LIMIT 10;


-- QUERY 3: Resumo Executivo: Distribuição de Severidade por Serviço (Logs de Aplicação)
SELECT 
    service_name,
    level,
    COUNT(*) AS quantidade_logs,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY service_name), 2) AS percentual
FROM lumina_logs_db.logs_silver_parquet
WHERE source IN ('backend', 'containers')
GROUP BY service_name, level
ORDER BY service_name, quantidade_logs DESC;


-- QUERY 4: Diagnóstico de Erros Críticos no Backend Spring Boot (Últimas Exceções)
SELECT 
    event_timestamp,
    logger_class,
    message
FROM lumina_logs_db.logs_silver_parquet
WHERE source = 'backend'
  AND level = 'ERROR'
ORDER BY event_timestamp DESC
LIMIT 50;


-- QUERY 5: Volumetria e Saúde do Pipeline por Dia e por Fonte
SELECT 
    year,
    month,
    day,
    source,
    COUNT(*) AS total_registros
FROM lumina_logs_db.logs_silver_parquet
GROUP BY year, month, day, source
ORDER BY year DESC, month DESC, day DESC, total_registros DESC;
