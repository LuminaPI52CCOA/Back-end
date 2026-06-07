# 🚀 Lumina API - Back-end

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?style=for-the-badge&logo=swagger)](https://swagger.io/)

O **Lumina API** é o core operacional do ecossistema Lumina, uma plataforma desenvolvida para otimizar a gestão de clínicas e bem-estar. Este repositório concentra toda a inteligência de negócios, persistência de dados, segurança e integrações da aplicação.

---

## 🛠️ Tecnologias e Ferramentas

O projeto foi construído utilizando as melhores práticas de desenvolvimento de software, com foco em escalabilidade e manutenibilidade:

*   **Linguagem Principal:** Java 17
*   **Framework:** Spring Boot 3.x
    *   *Spring Data JPA* (Abstração de banco de dados e repositórios)
    *   *Spring Web* (Construção de APIs RESTful)
*   **Documentação:** SpringDoc OpenAPI / Swagger UI
*   **Gerenciamento de Dependências:** Maven
*   **Ambiente de Desenvolvimento:** IntelliJ IDEA Ultimate

---

## 📐 Arquitetura do Projeto

O código está organizado seguindo o padrão de camadas clássico para APIs REST:

```text
src/main/java/com/lumina/backend
├── config/          # Configurações globais (Swagger, Segurança, etc.)
├── controllers/     # Endpoints expostos pela API (Portas de entrada)
├── dtos/            # Data Transfer Objects (Validação e transferência)
├── models/          # Entidades de domínio (Mapeamento ORM/JPA)
├── repositories/    # Interfaces de comunicação com o banco de dados
└── services/        # Camada de regras de negócio e lógica da aplicação
