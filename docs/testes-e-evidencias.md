# Testes e evidências

Comando executado:

```powershell
.\mvnw.cmd test
```

Resultado atual: `clean verify` concluído com sucesso; 18 testes (unitários e integração)
executados sem falhas; relatório JaCoCo gerado em `target/site/jacoco`; o gate de linhas de domínio e
aplicação de 80% foi aprovado (cobertura do relatório: 89,71%).

O cenário ponta a ponta entre serviços, Testcontainers, contrato de broker e quality
gate Sonar não podem ser declarados concluídos sem ambiente e contratos dos demais
serviços. O teste de persistência usa H2 isolado; a aplicação mantém `ddl-auto: validate`
e migrations Flyway para ambientes reais, mas a execução de migrations contra PostgreSQL
deve ser validada no CI/ambiente de banco do serviço.

## BDD

Foi adicionada uma especificação Cucumber executável em
`src/test/resources/features/execution-flow.feature`. O fluxo cobre:

1. autorização da OS;
2. início do diagnóstico;
3. início do reparo;
4. conclusão da execução;
5. histórico completo e evento `execution.completed` no outbox.

O cenário usa MockMvc e repositórios isolados no contexto Spring de teste, portanto não
exige MongoDB ou RabbitMQ para execução local. Para executar somente o BDD:

```powershell
.\mvnw.cmd -B -Dtest=CucumberTest test
```

O `mvnw verify` continua sendo a validação recomendada para executar o BDD junto dos
testes existentes, do relatório JaCoCo e do gate de cobertura.
