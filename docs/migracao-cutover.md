# Migração e cutover

1. Validar os contratos provisórios com OS e Billing.
2. Provisionar banco próprio e aplicar migrations.
3. Executar testes de contrato e BDD distribuído em homologação.
4. Roteiar novas autorizações para o Execution Service com feature flag ou adapter.
5. Confirmar que apenas um componente escreve o estado de execução.
6. Monitorar outbox, erros e duplicatas durante a transição.
7. Só depois discutir a desativação das operações legadas no monólito.

Nenhuma operação do monólito foi removida ou alterada nesta implementação.
