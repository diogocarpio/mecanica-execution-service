# Arquitetura proposta

## Fronteira

O Execution Service será dono apenas da execução técnica: fila operacional, progresso
interno de diagnóstico/reparos, conclusão técnica e notificações de integração. O OS
Service continuará dono da identidade, status global e histórico geral da OS. Billing
continuará dono de orçamento, aprovação e pagamento.

O `osId` será uma referência externa estável. O Execution Service não poderá consultar ou
alterar diretamente tabelas, entidades ou repositórios dos outros serviços.

## Camadas

```text
config
  Spring, RabbitMQ, clock e wiring
controller
  controllers HTTP e tratamento de erros
domain
  entidades, estados e regras de negócio
dto
  contratos de entrada, saída e eventos
exception
  exceções de domínio e contratos
listener
  consumidores RabbitMQ e publicadores outbox
mapper
  conversão entre domínio e DTO
repository
  persistência MongoDB
servide
  casos de uso da aplicação
util
  utilitários compartilhados
validation
  validação de mensagens recebidas
```

O domínio não dependerá de HTTP ou RabbitMQ. DTOs são convertidos por `mapper`, e a
persistência MongoDB fica isolada em `repository`.

## Fluxo provisório de integração

Até a confirmação do grupo, o serviço usará contratos versionados provisórios para:

1. autorizar uma execução;
2. consultar/iniciar o trabalho local;
3. registrar conclusão;
4. solicitar cancelamento quando a compensação for possível.

Esses contratos serão substituíveis sem alterar o domínio. A integração real só será
declarada após validação com OS/Billing e execução ponta a ponta.

## Consistência

Mudança de estado local e evento de saída deverão ser persistidos na mesma transação
local. O publicador será assíncrono e tolerará reentrega; consumidores deverão deduplicar
por `eventId` ou identificador de comando. Nenhuma dessas garantias equivale a rollback
distribuído ou desfaz reparo físico.

## Estado atual versus alvo

| Origem | Destino |
|---|---|
| `OrdemServico.status` global | `Execution.executionStatus` local |
| atualização genérica via `PATCH` | ações de domínio com transições explícitas |
| notificação que escreve no banco da OS | evento/contrato para o OS Service |
| ordenação em query da OS | fila local de execução, com regra documentada |
| ausência de histórico | histórico local de transições |
| ausência de mensageria | inbox/outbox e publicador confiável |

## Decisões ainda abertas

- broker e topologia;
- estratégia global da Saga;
- estados internos e momento exato do diagnóstico;
- autenticação entre serviços;
- banco definitivo e mecanismo de migrations;
- contrato final com OS/Billing.
