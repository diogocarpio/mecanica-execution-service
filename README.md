# Mecanica Execution Service

Microsserviço responsável pela execução técnica das ordens de serviço. A implementação
está sendo extraída do monólito de forma incremental; contratos de integração ainda
provisórios não representam o contrato final do grupo.

## Responsabilidades

- Gerenciar fila de execução das ordens de serviço
- Atualizar status durante diagnóstico
- Atualizar status durante reparos
- Notificar OS Service quando o serviço estiver concluído

## Tecnologias

- Java 17
- Spring Boot 4.0.5
- MongoDB (NoSQL)
- RabbitMQ (`os.exchange`, exchange `topic`)
- Docker
- Kubernetes
- Terraform

## Estrutura do Repositório

```text
src/main/java/com/f1rsters/execution/
├── config/                 # configuração Spring, RabbitMQ e clock
├── controller/             # endpoints HTTP e tratamento de erros
├── domain/                 # entidades, estados e regras de negócio
├── dto/                    # contratos de entrada, saída e eventos
├── exception/              # exceções de domínio e contratos
├── listener/               # consumidores RabbitMQ e publicadores outbox
├── mapper/                 # conversão domínio/DTO
├── repository/             # repositórios MongoDB
├── servide/                # casos de uso da aplicação
├── util/                   # utilitários compartilhados
└── validation/             # validação de eventos recebidos
docs/                       # inventário, arquitetura e pendências
```

## Endpoints provisórios

- `POST /api/executions/authorization` cria ou retorna a execução da OS (`osId`)
- `GET /api/executions/queue` consulta a fila FIFO por aprovação
- `POST /api/executions/{executionId}/complete` conclui a execução localmente

Esses endpoints serão revisados quando os contratos OS/Billing forem formalizados.

## Próximos passos

1. Validar o contrato final de eventos com o OS Service.
2. Executar MongoDB e RabbitMQ no ambiente de integração.
3. Adicionar testes ponta a ponta e BDD distribuído.
4. Configurar CI/CD, DLQ e observabilidade operacional.

## Execução local

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

## Status

⚠️ **Persistência MongoDB e topologia RabbitMQ foram implementadas; cutover distribuído ainda depende da validação dos contratos.**
