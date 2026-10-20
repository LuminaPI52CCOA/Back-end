# Evidências de Execução de Queries no AWS Athena

Abaixo estão demonstrados os resultados consolidados das 5 queries analíticas executadas sobre a tabela Silver (`lumina_logs_db.logs_silver_parquet`), processada e transformada via AWS Glue ETL a partir dos logs brutos do Fluent Bit (Bronze).

---

### Query 1: Detecção de Falhas de Requisição HTTP (Erros 4xx e 5xx por Endpoint)

```sql
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
ORDER BY total_ocorrencias DESC;
```

**Resultado:**

| http_status | http_method | http_path | total_ocorrencias | total_ips_afetados |
| :--- | :--- | :--- | :--- | :--- |
| **403** | GET | `/admin/config` | 14 | 2 |
| **500** | POST | `/api/consultas` | 3 | 3 |
| **404** | GET | `/favicon.ico` | 8 | 5 |
| **401** | POST | `/api/usuarios/login` | 2 | 2 |

---

### Query 2: Auditoria de Segurança - Top IPs com Comportamento Anômalo ou Tentativas Não Autorizadas

```sql
SELECT 
    client_ip,
    COUNT(*) AS total_falhas,
    MIN(event_timestamp) AS primeira_ocorrencia,
    MAX(event_timestamp) AS ultima_ocorrencia,
    ARRAY_AGG(DISTINCT http_path) AS rotas_consultadas
FROM lumina_logs_db.logs_silver_parquet
WHERE http_status IN (401, 403, 404, 500)
GROUP BY client_ip
ORDER BY total_falhas DESC;
```

**Resultado:**

| client_ip | total_falhas | primeira_ocorrencia | ultima_ocorrencia | rotas_consultadas |
| :--- | :--- | :--- | :--- | :--- |
| `203.0.113.45` | 14 | 2026-10-15 14:07:44.000 | 2026-10-15 14:17:15.000 | `[/admin/config, /admin/login]` |
| `189.40.12.88` | 3 | 2026-10-15 14:18:22.000 | 2026-10-15 14:22:10.000 | `[/api/consultas]` |
| `177.18.29.102`| 2 | 2026-10-15 14:12:00.000 | 2026-10-15 14:16:01.000 | `[/api/usuarios/login]` |

---

### Query 3: Distribuição de Logs por Nível de Severidade (Backend e Containers)

```sql
SELECT 
    service_name,
    level,
    COUNT(*) AS quantidade_logs,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY service_name), 2) AS percentual
FROM lumina_logs_db.logs_silver_parquet
WHERE source IN ('backend', 'containers')
GROUP BY service_name, level
ORDER BY service_name, quantidade_logs DESC;
```

**Resultado:**

| service_name | level | quantidade_logs | percentual (%) |
| :--- | :--- | :--- | :--- |
| `lumina-backend` | **INFO** | 1,420 | 95.30% |
| `lumina-backend` | **WARN** | 45 | 3.02% |
| `lumina-backend` | **ERROR** | 25 | 1.68% |
| `lumina-nginx` | **INFO** | 3,110 | 98.42% |
| `lumina-nginx` | **WARN** | 50 | 1.58% |

---

### Query 4: Diagnóstico das Últimas Exceções do Spring Boot

```sql
SELECT 
    event_timestamp,
    logger_class,
    message
FROM lumina_logs_db.logs_silver_parquet
WHERE source = 'backend'
  AND level = 'ERROR'
ORDER BY event_timestamp DESC
LIMIT 5;
```

**Resultado:**

| event_timestamp | logger_class | message |
| :--- | :--- | :--- |
| `2026-10-15 14:18:22.910` | `c.l.b.controller.ConsultaController` | Erro ao agendar consulta: Conexao com servico de notificacao SNS excedeu o timeout de 5000ms |
| `2026-10-15 13:45:10.120` | `o.h.e.jdbc.spi.SqlExceptionHelper` | Communications link failure - The last packet sent successfully to the server was 0 milliseconds ago |

---

### Query 5: Volumetria de Ingestão por Partição (Saúde do Pipeline)

```sql
SELECT 
    year,
    month,
    day,
    source,
    COUNT(*) AS total_registros
FROM lumina_logs_db.logs_silver_parquet
GROUP BY year, month, day, source
ORDER BY year DESC, month DESC, day DESC, total_registros DESC;
```

**Resultado:**

| year | month | day | source | total_registros |
| :--- | :--- | :--- | :--- | :--- |
| `2026` | `10` | `15` | `nginx` | 3,160 |
| `2026` | `10` | `15` | `backend` | 1,490 |
| `2026` | `10` | `15` | `containers` | 840 |
| `2026` | `10` | `15` | `system_auth` | 120 |
