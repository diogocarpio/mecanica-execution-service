# Inventário de extração

## Contexto verificado

Origem: `f1rsters-tech-challenge-mecanica`  
Destino: `mecanica-execution-service`

O baseline da origem foi executado com `mvnw.cmd test` sem alterações no repositório e
terminou com exit code 0. A origem usa Java 17, Spring Boot 4.0.5, Spring MVC, Spring
Data JPA, PostgreSQL em produção, H2 em testes, JWT/Spring Security, Actuator,
Micrometer/New Relic e Maven.

## Mapa de capacidades

| Funcionalidade | Entrada atual | Caso de uso atual | Domínio/persistência | Testes | Decisão |
|---|---|---|---|---|---|
| Criação da OS | `POST /api/admin/ordens-servico` em `controller/OrdemServicoController.java` | `OrdemServicoService.criarOrdem` | `OrdemServico`, `Cliente`, `Veiculo`, `Servico`, `Peca`; `OrdemServicoRepository` e repositórios relacionados | `OrdemServicoServiceTest`, `OrdemServicoControllerTest` | PERMANECER no OS Service |
| Consulta/listagem da OS | `GET /api/admin/ordens-servico`, `/{id}`, `/{id}/status` | `listarTodas`, `detalhar`, `consultarStatus` | `OrdemServicoRepository.findAllActiveOrderByStatusAndDate` | `OrdemServicoServiceTest`, `OrdemServicoControllerTest` | PERMANECER; criar apenas consulta local de execução |
| Ordenação operacional | Derivada de `listarTodas` | `findAllActiveOrderByStatusAndDate` | Ordem `EM_EXECUCAO`, `AGUARDANDO_APROVACAO`, `DIAGNOSTICO`, `RECEBIDA`, depois `criadoEm` | `OrdemServicoServiceTest` | EXTRAIR como regra candidata; validar se a fila pertence ao Execution |
| Aprovação de orçamento | `POST /api/admin/ordens-servico/{id}/orcamento/resposta` | `processarRespostaOrcamento` | Transição `AGUARDANDO_APROVACAO` → `EM_EXECUCAO` | `OrdemServicoServiceTest`, `OrdemServicoControllerTest` | PERMANECER no Billing/OS; Execution consumirá autorização |
| Atualização por notificação | `POST /api/admin/ordens-servico/{id}/status/notificacao` | `processarNotificacaoStatus` | Atualiza diretamente `OrdemServico.status` | `OrdemServicoServiceTest`, `OrdemServicoControllerTest` | REDESENHAR como contrato; não copiar escrita no status global |
| Atualização manual de status | `PATCH /api/admin/ordens-servico/{id}/status` | `atualizarStatus` | Persiste qualquer `StatusOrdemServico` sem validar matriz | `OrdemServicoServiceTest`, `OrdemServicoControllerTest` | PERMANECER no legado durante transição; não transportar sem regras |
| Diagnóstico e reparos | Não há entidade, caso de uso ou endpoint específico encontrado | Não há operação dedicada | Apenas os valores `DIAGNOSTICO` e `EM_EXECUCAO` no enum | Cobertos indiretamente por atualização/notificação | REDESENHAR após contrato de domínio; não inventar granularidade |
| Conclusão | Status `FINALIZADA`/`ENTREGUE` via atualização genérica | Não há caso de uso de conclusão dedicado | Sem histórico, outbox ou evento de conclusão | Sem teste de notificação para OS Service | REDESENHAR no Execution com conclusão idempotente e outbox |
| Consulta pública | `GET /api/public/ordens-servico/{id}` | `getPublicInfo` | Faz join de cliente, veículo, serviços e peças | `OrdemServicoServiceTest`, controller público | PERMANECER no OS Service |

## Estados confirmados na origem

`StatusOrdemServico` contém:

```text
RECEBIDA
DIAGNOSTICO
AGUARDANDO_APROVACAO
EM_EXECUCAO
FINALIZADA
ENTREGUE
```

O código atual permite atualização genérica de status e não implementa uma máquina de
transições explícita. Portanto, a sequência entre diagnóstico, aprovação, execução e
reparo não pode ser inferida apenas do enum. O Execution Service deverá definir uma
máquina interna somente após o contrato do grupo; o mapeamento para o status global será
feito por integração.

## Limites de dados

- `OrdemServico`, cliente, veículo, catálogo e estoque permanecem fora do banco do
  Execution Service.
- `osId` será referência externa; não haverá entidade JPA ou foreign key para o banco
  do OS Service.
- O destino precisará de persistência própria para execução, histórico, deduplicação e
  eventos pendentes quando esses fluxos forem implementados.
- A origem usa `ddl-auto: update` e H2 `create-drop` nos testes; não há Flyway/Liquibase
  identificado. O destino deverá usar migrations versionadas, sem depender de
  `ddl-auto=create`.

## Lacunas e dependências

1. Não foram encontrados endpoints específicos de diagnóstico ou reparo na origem.
2. Não foi encontrada mensageria implementada na origem; RabbitMQ aparece apenas como
   tecnologia planejada no README do destino.
3. Não há contrato confirmado para autorização da execução, conclusão, cancelamento ou
   compensação.
4. Não há histórico de transições, inbox/outbox, controle de concorrência ou evento de
   conclusão no monólito.
5. O banco SQL é confirmado na origem; o requisito global de NoSQL depende dos demais
   serviços.

## Evidências principais

- `f1rsters-tech-challenge-mecanica/src/main/java/com/f1rsters/tech_challenge_mecanica/service/OrdemServicoService.java`
- `f1rsters-tech-challenge-mecanica/src/main/java/com/f1rsters/tech_challenge_mecanica/controller/OrdemServicoController.java`
- `f1rsters-tech-challenge-mecanica/src/main/java/com/f1rsters/tech_challenge_mecanica/domain/StatusOrdemServico.java`
- `f1rsters-tech-challenge-mecanica/src/main/java/com/f1rsters/tech_challenge_mecanica/repository/OrdemServicoRepository.java`
- `f1rsters-tech-challenge-mecanica/src/test/java/com/f1rsters/tech_challenge_mecanica/service/OrdemServicoServiceTest.java`
- `f1rsters-tech-challenge-mecanica/src/test/java/com/f1rsters/tech_challenge_mecanica/controller/OrdemServicoControllerTest.java`
