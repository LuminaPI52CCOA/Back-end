# 🚀 Lumina API — Back-end

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?style=for-the-badge&logo=swagger)](https://swagger.io/)

O **Lumina API** é o núcleo de regras de negócio, segurança, persistência e integrações externas do ecossistema **Lumina Odontológica**. A aplicação foi desenvolvida em Java 21 com Spring Boot 3, oferecendo APIs RESTful de alta performance, auditoria de ações clínicas, inteligência artificial generativa e conectividade com a assistente virtual Alexa.

---

## 🛠️ Tecnologias e Recursos

* **Linguagem Principal:** Java 21 (LTS)
* **Framework:** Spring Boot 3.x
  * **Spring Security & JJWT:** Autenticação stateless via Bearer Token e cookies seguros `HttpOnly` com proteção CSRF.
  * **Spring Data JPA & Hibernate:** Mapeamento objeto-relacional para MySQL.
  * **Spring Scheduling:** Tarefas proativas em segundo plano (`@Scheduled`) com fuso horário `America/Sao_Paulo`.
  * **SpringDoc OpenAPI:** Documentação interativa Swagger UI.
* **Inteligência Artificial:** Google Gemini AI API (geração de resumos de anamnese e alertas clínicos).
* **Integração de Voz:** Amazon Alexa Skills e Login with Amazon (LWA OAuth2).
* **Build e Testes:** Apache Maven, JUnit 5 e Mockito (127+ testes unitários e de integração).

---

## 📐 Estrutura de Pacotes

```text
src/main/java/com/lumina/backend
├── config/              # Configurações de segurança, CORS, Swagger e beans globais
├── controller/          # Controladores REST expostos pela API (endpoints)
├── dto/                 # Data Transfer Objects com validação Jakarta Bean Validation
├── exception/           # Tratamento centralizado de exceções (GlobalExceptionHandler)
├── model/               # Entidades de domínio JPA mapeadas para o banco Lumina
├── repository/          # Repositórios Spring Data com consultas customizadas
├── service/             # Regras de negócio, serviços clínicos e integrações
│   ├── alexa/           # Pareamento por PIN, desvinculação e Alexa Reminders Push
│   └── openIA/          # Integração com a API do Google Gemini AI
└── swagger/             # Utilitários de geração e validação de tokens JWT
```

---

## 🔑 Principais Módulos da API

### 1. Autenticação e Usuários (`/usuarios`)
* `POST /usuarios/login`: Autentica dentistas e recepcionistas, retornando JWT e setando cookie de sessão.
* `POST /usuarios`: Cadastro de novos profissionais e controle de acessos por perfil (`ROLE_ADMIN`, `ROLE_DENTISTA`).
* `POST /usuarios/logout`: Invalidação e limpeza segura da sessão do usuário.

### 2. Pacientes e Prontuários (`/clientes`)
* `GET /clientes`: Listagem paginada de pacientes com filtros por nome e CPF.
* `POST /clientes`: Cadastro de paciente com vínculo de convênio e responsável legal.
* `GET /clientes/estado-civil`: Domínio dos estados civis cadastrados.

### 3. Consultas e Agenda (`/consultas`)
* `GET /consultas`: Grade de agendamentos com filtros por data, status e dentista.
* `POST /consultas`: Agendamento de novos procedimentos com detecção de conflitos de horário.

### 4. Integração Alexa (`/alexa`)
* `POST /alexa/gerar-pin`: Gera um código temporário de 6 dígitos (válido por 10 min) para vincular o Echo.
* `GET /alexa/status`: Retorna o status de conexão do dentista autenticado com o dispositivo Echo.
* `POST /alexa/vincular`: Endpoint chamado pela AWS Lambda para concluir o pareamento seguro do PIN.
* `DELETE /alexa/desconectar`: Desvincula o dispositivo físico da conta do dentista.
* **Lembretes Proativos (Scheduler):** O serviço `AlexaReminderService` varre o banco a cada 60s em busca de consultas que iniciam em ~10 minutos e dispara alertas de voz proativos nos dispositivos vinculados.

---

## ⚙️ Variáveis de Ambiente e Configuração

O arquivo `src/main/resources/application.properties` é ignorado no Git por segurança. Para configurar a aplicação, utilize variáveis de ambiente ou copie o modelo:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### Variáveis Suportadas:

| Variável | Padrão | Descrição |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:mysql://localhost:3306/Lumina` | URL de conexão JDBC com o MySQL |
| `DB_USERNAME` | `root` | Usuário de acesso ao banco |
| `DB_PASSWORD` | `2741` | Senha de acesso ao banco |
| `JWT_SECRET` | *(chave Base64)* | Chave HMAC-SHA256 para assinatura dos tokens JWT |
| `JWT_VALIDITY` | `3600` | Validade do token em segundos (1 hora) |
| `GEMINI_API_KEY` | *(opcional)* | Chave de API do Google Gemini para IA |
| `ALEXA_LWA_CLIENT_ID` | `""` | Client ID do Login with Amazon (LWA) para Reminders |
| `ALEXA_LWA_CLIENT_SECRET` | `""` | Client Secret do Login with Amazon (LWA) |
| `ALEXA_REMINDERS_ENABLED`| `true` | Habilita/desabilita o agendador de lembretes da Alexa |

---

## 💻 Como Rodar Localmente

### Pré-requisitos:
* Java 21 JDK instalado (`java -version`)
* Maven 3.8+ instalado (`mvn -version`)
* MySQL 8.0 em execução com o schema `Lumina`

### Executar a aplicação:
```bash
mvn clean spring-boot:run
```

A API estará disponível em: `http://localhost:8080`  
Documentação Swagger UI: `http://localhost:8080/swagger-ui.html`

### Executar testes:
```bash
mvn test
```
