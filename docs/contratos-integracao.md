# Contratos de integração provisórios

Os contratos abaixo destravam o desenvolvimento local e ainda precisam ser validados
pelo grupo. Eles não autorizam o cutover da origem.

## Autorização

`POST /api/executions/authorization`

```json
{"osId": 123}
```

É idempotente por `osId`: chamadas repetidas retornam a mesma execução ativa.

## Operações técnicas

- `POST /api/executions/{executionId}/diagnosis/start`
- `POST /api/executions/{executionId}/repairs/start`
- `POST /api/executions/{executionId}/complete`
- `POST /api/executions/{executionId}/cancel`
- `GET /api/executions/{executionId}/history`

As transições inválidas retornam `409`; execução inexistente retorna `404`.

## Evento de conclusão

O registro `execution.completed` é criado em `outbox_events` junto da conclusão local.
A publicação fica desabilitada por padrão (`BROKER_ENABLED=false`); com
`BROKER_ENABLED=true`, o adapter RabbitMQ publica na exchange `os.exchange` usando
`execution.completed` como routing key.
O evento só é marcado como publicado depois do retorno bem-sucedido do publisher.

## Saga provisória via RabbitMQ

- OS Service → `os.approved`: cria ou inicia uma execução usando `osId` e `eventId`.
- OS Service → `os.cancelled`: cancela a execução pelo `osId` e `eventId`.
- Execution → `os.status.updated`: informa diagnóstico e reparo ao OS Service.
- Execution → `execution.completed`: informa conclusão ao OS Service.

As mensagens usam o envelope `OSEvent` com `eventId`, `eventType`, `timestamp`, `osId`
e os dados da OS. O inbox impede reprocessamento do mesmo `eventId`. A topologia ainda
deve ser validada com o grupo junto da política de retry/DLQ.
