# Decisões e pendências

## Decisões tomadas nesta etapa

- A origem não será alterada durante o inventário.
- O destino terá banco e ciclo de build próprios.
- A referência entre serviços será `osId`, sem relacionamento ORM entre bancos.
- Contratos provisórios podem destravar o desenvolvimento, mas devem ser marcados como
  provisórios e substituíveis.
- A origem será preservada até existir cutover testado e plano de reversão.
- A implementação de mensageria usa RabbitMQ quando `BROKER_ENABLED=true`, com a
  exchange `os.exchange` do tipo `topic` e eventos `os.approved`, `os.cancelled`,
  `os.status.updated` e `execution.completed`.
- O fluxo da Saga é: comando de autorização externo → execução local → evento de
  conclusão para o OS Service; cancelamento antes do início é a compensação local.

## Pendências externas

| Pendência | Impacto | Critério de resolução |
|---|---|---|
| Contrato de autorização de execução | Define entrada e elegibilidade | Filas e envelope provisórios implementados; schema/emissor finais ainda precisam ser validados pelo grupo |
| Contrato de conclusão para OS Service | Define atualização do status global | `execution.completed` implementado via outbox/Rabbit; consumidor final ainda precisa ser validado |
| Estratégia Saga | Define comandos, eventos e compensações | Fluxo local implementado; escolha global entre orquestração/coreografia ainda precisa ser documentada pelo grupo |
| Momento do diagnóstico | Define máquina de estados | Fluxo real acordado com OS/Billing |
| Broker e topologia | Define listeners, configuração e operação | Tecnologia comum e DLQ/retry confirmados |
| Autenticação entre serviços | Define proteção das entradas | JWT/mTLS/gateway e roles confirmados |
| NoSQL global | Requisito da solução completa | Serviço responsável identificado no diagrama global |
| BDD ponta a ponta | Critério de aceite global | Ambiente integrado e cenário executável disponíveis |
