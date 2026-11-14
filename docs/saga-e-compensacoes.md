# Saga e compensações

O serviço participa provisoriamente como executor. A autorização representa um comando
externo; a conclusão é um fato local que será publicado para o OS Service.

O desenho usa compensação sem rollback distribuído:

- antes do início, `cancel` marca a execução como `CANCELLED` e impede início posterior;
- após diagnóstico/reparo iniciado, a compensação de trabalho físico não é tentada por
  software; o estado deve ser escalado ao coordenador;
- conclusão repetida permanece idempotente e não cria novo evento de conclusão;
- `eventId` do outbox será a chave de deduplicação no contrato final.

A estratégia global (orquestrada ou coreografada), broker, DLQ e política de retry
continuam pendentes de validação do grupo.
