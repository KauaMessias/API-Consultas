# API de Consultas Médicas

API REST para gerenciamento de consultas médicas, desenvolvida com Java e Spring Boot.
O sistema permite o cadastro de clientes e médicos, autenticação com JWT, controle de acesso por role e por vínculo com o recurso, gerenciamento de horários de atendimento, agendamento de consultas e busca de médicos e horários disponíveis.

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Security
- JWT
- Refresh Token com cookie HttpOnly
- JPA / Hibernate
- PostgreSQL
- Docker
- Docker Compose
- Bean Validation
- Swagger / OpenAPI
- JUnit
- Mockito
- MockMvc
- DataJpaTest

## Funcionalidades

- Cadastro de clientes
- Cadastro de médicos
- Login com access token JWT
- Refresh token com rotação
- Logout com revogação do refresh token
- Controle de acesso por roles e por vínculo com o recurso
- Cadastro e gerenciamento de endereços
- Cadastro de horários de atendimento
- Busca de horários disponíveis por médico e data
- Agendamento de consultas
- Atualização e cancelamento de consultas
- Tratamento global de exceções
- Testes automatizados

## Regras de negócio

- Clientes podem visualizar e alterar apenas seus próprios dados.
- Médicos podem visualizar dados de clientes relacionados às suas consultas.
- Consultas não podem ser marcadas em horários já ocupados.
- Horários disponíveis são gerados com base na agenda semanal do médico.
- Usuários inativos não podem acessar recursos protegidos.
- Refresh tokens são armazenados por hash e revogados após uso.

## Autenticação

A API utiliza autenticação baseada em JWT.
Após o login, o backend retorna um access token no corpo da resposta e envia o refresh token em um cookie HttpOnly.
O access token é usado para acessar rotas protegidas.
O refresh token é usado para renovar a sessão sem exigir novo login, utilizando rotação de tokens.

## Como rodar o projeto

### Pré-requisitos

- Docker
- Docker Compose
- Arquivo `.env` configurado
- JDK 21, caso deseje executar os testes localmente

## Variáveis de ambiente

Antes de subir a aplicação, crie um arquivo `.env` na raiz do projeto.

Exemplo:
```env
POSTGRES_DB=consultas_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=sua_chave_secreta
FRONTEND_URL=http://localhost:5173
```

### Subir a aplicação
Depois, execute:
```bash
docker compose up --build
```

A API ficará disponível em:
```text
http://localhost:8080
```

A documentação Swagger ficará disponível em:
```text
http://localhost:8080/swagger-ui/index.html
```

### Parar os containers
```bash
docker compose down
```

### Rodar em segundo plano
```bash
docker compose up -d --build
```

### Ver logs
```bash
docker compose logs -f
```

## Testes
Para executar os testes localmente:
```bash
./mvnw test
```

Ou, caso prefira executar dentro do container:
```bash
docker compose exec backend ./mvnw test
```

> Observação: substitua `backend` pelo nome do serviço definido no seu `docker-compose.yml`, se for diferente.

## Principais endpoints

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Realiza login |
| POST | `/api/v1/auth/refresh` | Renova o access token |
| POST | `/api/v1/auth/logout` | Realiza logout |

### Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/clientes` | Cadastra cliente |
| GET | `/api/v1/clientes/{id}` | Busca cliente por ID |
| PUT | `/api/v1/clientes/{id}` | Atualiza cliente |
| DELETE | `/api/v1/clientes/{id}` | Desativa cliente |

### Médicos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/medicos` | Cadastra médico |
| GET | `/api/v1/medicos` | Lista médicos |
| GET | `/api/v1/medicos/{id}` | Busca médico por ID |
| PUT | `/api/v1/medicos/{id}` | Atualiza médico |
| DELETE | `/api/v1/medicos/{id}` | Desativa médico |

### Consultas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/consultas` | Cria consulta |
| GET | `/api/v1/consultas/{id}` | Busca consulta por ID |
| PUT | `/api/v1/consultas/{id}` | Atualiza consulta |
| PATCH | `/api/v1/consultas/{id}` | Altera status da consulta |

### Horários

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/medicos/horarios` | Cadastra horário médico |
| GET | `/api/v1/medicos/{id}/horarios` | Lista horários do médico |
| GET | `/api/v1/medicos/{id}/horarios/disponiveis` | Busca horários disponíveis |

## Estrutura do projeto

```text
src
 └── main
     └── java
         └── com.example.consultas
             ├── controllers
             ├── services
             ├── repositories
             ├── models
             ├── dtos
             ├── exceptions
             └── security
```

## Testes implementados

O projeto possui testes para diferentes camadas da aplicação:
- Controllers com MockMvc
- Services com Mockito
- Repositories com DataJpaTest

Os testes verificam fluxos de sucesso, erros de validação, exceções de negócio e consultas ao banco.

## Deploy

O frontend integrado a esta API está disponível em:
https://front-consultas.vercel.app/login

Repositório do frontend:
https://github.com/KauaMessias/Front-Consultas

## Objetivo do projeto

Este projeto foi desenvolvido como projeto de portfólio backend, com foco em construção de API REST, autenticação, autorização, organização em camadas, regras de negócio e testes automatizados.

## Autor

**Kauã Santos**

- GitHub: [@KauaMessias](https://github.com/KauaMessias)
- LinkedIn: [Kauã Messias](https://www.linkedin.com/in/kauã-messias-413229341)
- Email: kauamessias1@gmail.com
