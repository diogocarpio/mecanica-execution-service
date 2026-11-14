# Relatório de eventos RabbitMQ

Este documento descreve a integração RabbitMQ implementada pelo **OS Service**. Ele pode ser usado como referência para implementar um microsserviço consumidor ou produtor compatível.

## 1. Configuração da mensageria

| Item | Valor |
|---|---|
| Exchange | `os.exchange` |
| Tipo da exchange | `topic` |
| Host padrão | `localhost` |
| Porta padrão | `5672` |
| Conversor | `Jackson2JsonMessageConverter` |
| Formato da mensagem | JSON |

Todas as mensagens são publicadas na exchange `os.exchange`. O `routing key` é o próprio nome do evento.

## 2. Topologia de filas e tópicos

| Fila | Routing key | Direção | Serviço esperado |
|---|---|---|---|
| `os.created.queue` | `os.created` | Saída do OS Service | Billing Service |
| `os.approved.queue` | `os.approved` | Saída do OS Service | Execution Service |
| `os.status.updated.queue` | `os.status.updated` | Saída do OS Service | Monitoramento/integrações |
| `billing.orcamento.aprovado.queue` | `orcamento.aprovado` | Entrada do OS Service | Billing Service |
| `billing.orcamento.rejeitado.queue` | `orcamento.rejeitado` | Entrada do OS Service | Billing Service |
| `execution.completed.queue` | `execution.completed` | Entrada do OS Service | Execution Service |
| `os.cancelled.queue` | `os.cancelled` | Entrada e saída do fluxo de compensação | Billing/Execution/OS Service |

As filas são duráveis e estão vinculadas à exchange `os.exchange` com os respectivos `routing keys`.

## 3. Estrutura comum do objeto JSON

Todos os eventos usam o objeto `OSEvent`:

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "EM_EXECUCAO",
  "eventType": "os.approved",
  "timestamp": "2026-11-11T22:00:00"
}
```

### Campos

| Campo | Tipo JSON | Obrigatório no contrato | Descrição |
|---|---|---|---|
| `osId` | number inteiro | Sim | Identificador da ordem de serviço |
| `clienteId` | string | Sim | Identificador externo do cliente, normalmente CPF/CNPJ normalizado |
| `veiculoId` | string | Sim | Identificador externo do veículo, normalmente a placa normalizada |
| `servicosIds` | array de strings | Sim | IDs dos serviços solicitados |
| `pecasIds` | array de strings | Sim | IDs das peças solicitadas |
| `valorTotal` | number decimal | Sim | Valor total do orçamento/OS |
| `status` | string | Sim | Status atual da ordem |
| `eventType` | string | Sim | Nome do evento; deve ser igual ao routing key |
| `timestamp` | string ISO-8601 | Sim | Data/hora de criação do evento |

O campo `timestamp` deve ser serializado como texto ISO-8601, por exemplo `2026-11-11T22:00:00`, e não como timestamp numérico.

## 4. Valores possíveis de `status`

```text
RECEBIDA
DIAGNOSTICO
AGUARDANDO_APROVACAO
EM_EXECUCAO
FINALIZADA
ENTREGUE
CANCELADA
```

## 5. Eventos publicados pelo OS Service

### 5.1 `os.created`

**Routing key:** `os.created`  
**Fila de destino:** `os.created.queue`  
**Consumidor esperado:** Billing Service  
**Quando ocorre:** depois que uma nova OS é persistida com status `RECEBIDA`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 0.00,
  "status": "RECEBIDA",
  "eventType": "os.created",
  "timestamp": "2026-11-11T22:00:00"
}
```

**Ação esperada do Billing Service:**

1. Consumir a mensagem.
2. Criar o orçamento relacionado ao `osId`.
3. Preencher o valor do orçamento.
4. Publicar `orcamento.aprovado` ou `orcamento.rejeitado`.

### 5.2 `os.approved`

**Routing key:** `os.approved`  
**Fila de destino:** `os.approved.queue`  
**Consumidor esperado:** Execution Service  
**Quando ocorre:** depois que o orçamento foi aprovado e a OS mudou para `EM_EXECUCAO`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "EM_EXECUCAO",
  "eventType": "os.approved",
  "timestamp": "2026-11-11T22:05:00"
}
```

**Ação esperada do Execution Service:**

1. Consumir a mensagem.
2. Criar ou iniciar a execução para o `osId`.
3. Ao concluir, publicar `execution.completed`.
4. Se houver cancelamento, consumir `os.cancelled` e interromper a execução pendente.

### 5.3 `os.status.updated`

**Routing key:** `os.status.updated`  
**Fila de destino:** `os.status.updated.queue`  
**Consumidor esperado:** monitoramento ou outros serviços de integração  
**Quando ocorre:** depois que a execução foi concluída e a OS mudou para `FINALIZADA`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "FINALIZADA",
  "eventType": "os.status.updated",
  "timestamp": "2026-11-11T22:20:00"
}
```

### 5.4 `os.cancelled`

**Routing key:** `os.cancelled`  
**Fila de destino:** `os.cancelled.queue`  
**Consumidores esperados:** Billing Service, Execution Service e integrações dependentes  
**Quando ocorre:** rejeição do orçamento, cancelamento manual ou compensação da Saga.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "CANCELADA",
  "eventType": "os.cancelled",
  "timestamp": "2026-11-11T22:07:00"
}
```

**Ação esperada dos consumidores:**

- Billing Service: cancelar o orçamento ou realizar o estorno lógico.
- Execution Service: cancelar a execução se ela ainda estiver em andamento.
- Outros serviços: encerrar operações relacionadas à OS.

## 6. Eventos consumidos pelo OS Service

O OS Service desserializa todas as mensagens abaixo como `OSEvent`.

### 6.1 `orcamento.aprovado`

**Fila consumida:** `billing.orcamento.aprovado.queue`  
**Routing key:** `orcamento.aprovado`  
**Produtor:** Billing Service  
**Efeito:** altera a OS para `EM_EXECUCAO` e publica `os.approved`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "AGUARDANDO_APROVACAO",
  "eventType": "orcamento.aprovado",
  "timestamp": "2026-11-11T22:04:00"
}
```

### 6.2 `orcamento.rejeitado`

**Fila consumida:** `billing.orcamento.rejeitado.queue`  
**Routing key:** `orcamento.rejeitado`  
**Produtor:** Billing Service  
**Efeito:** altera a OS para `CANCELADA` e publica `os.cancelled`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "AGUARDANDO_APROVACAO",
  "eventType": "orcamento.rejeitado",
  "timestamp": "2026-11-11T22:04:00"
}
```

### 6.3 `execution.completed`

**Fila consumida:** `execution.completed.queue`  
**Routing key:** `execution.completed`  
**Produtor:** Execution Service  
**Efeito:** altera a OS para `FINALIZADA` e publica `os.status.updated`.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "FINALIZADA",
  "eventType": "execution.completed",
  "timestamp": "2026-11-11T22:20:00"
}
```

### 6.4 `os.cancelled`

**Fila consumida:** `os.cancelled.queue`  
**Routing key:** `os.cancelled`  
**Produtor esperado:** serviço que iniciou a compensação  
**Efeito:** o listener do OS Service processa o evento e mantém/transiciona a OS para `CANCELADA`, quando a transição for válida.

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "CANCELADA",
  "eventType": "os.cancelled",
  "timestamp": "2026-11-11T22:07:00"
}
```

## 7. Fluxo completo da Saga

```text
1. Cliente cria a OS
2. OS Service publica os.created
3. Billing Service gera o orçamento
4. Billing Service publica orcamento.aprovado
5. OS Service muda para EM_EXECUCAO
6. OS Service publica os.approved
7. Execution Service executa o serviço
8. Execution Service publica execution.completed
9. OS Service muda para FINALIZADA
10. OS Service publica os.status.updated
```

Fluxo de rejeição:

```text
orcamento.rejeitado
  -> OS Service muda a OS para CANCELADA
  -> OS Service publica os.cancelled
  -> Billing/Execution cancelam operações pendentes
```

## 8. Regras para implementação do outro microsserviço

1. Usar a exchange `os.exchange` do tipo `topic`.
2. Usar exatamente os nomes dos `routing keys`.
3. Usar JSON com os mesmos nomes de campos e tipos.
4. Configurar suporte a `LocalDateTime` no formato ISO-8601.
5. Usar `eventType` igual ao `routing key` da mensagem.
6. Correlacionar todas as operações pelo campo `osId`.
7. Não criar um payload diferente para cada evento; reutilizar o contrato `OSEvent`.
8. Declarar filas próprias para o microsserviço consumidor, caso seja necessário isolamento de consumo. O RabbitMQ entrega uma mensagem a apenas um consumidor dentro da mesma fila.
9. Implementar idempotência usando `osId`, `eventType` e, se necessário, `timestamp` ou um identificador próprio da mensagem.
10. Configurar reconhecimento (`ack`) e tratamento de erro para impedir perda silenciosa de mensagens.

## 9. Eventos não implementados neste OS Service

`execution.failed` é mencionado na documentação geral como possível evento de falha, mas não existe listener nem binding para ele neste projeto.

Para compatibilidade com a implementação atual, o Execution Service deve publicar `os.cancelled` quando uma execução falhar, ou o OS Service deverá ser alterado para adicionar:

- fila para `execution.failed`;
- binding com routing key `execution.failed`;
- listener;
- transição para `CANCELADA`;
- publicação de `os.cancelled`.

## 10. Referências no código

- Topologia RabbitMQ: `src/main/java/com/f1rsters/os_service/config/RabbitMQConfig.java`
- Publicação de eventos: `src/main/java/com/f1rsters/os_service/service/OrdemServicoService.java`
- Consumo de eventos: `src/main/java/com/f1rsters/os_service/listener/OrdemServicoSagaListener.java`
- DTO compartilhado: `src/main/java/com/f1rsters/os_service/dto/OSEvent.java`
- Contrato funcional: `docs/contrato-eventos-saga.md`
- Integração entre microsserviços: `docs/integracao-microsservicos.md`
