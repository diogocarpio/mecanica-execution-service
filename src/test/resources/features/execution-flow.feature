# language: pt
Funcionalidade: Fluxo de execução de uma ordem de serviço
  Como sistema de execução
  Quero acompanhar a execução técnica de uma OS
  Para informar seu progresso e concluir o atendimento

  Cenário: concluir uma execução após diagnóstico e reparo
    Dado que não existe execução para a OS 123
    Quando autorizo a OS 123
    Então a execução deve estar no estado "QUEUED"
    E inicio o diagnóstico
    Então a execução deve estar no estado "IN_DIAGNOSIS"
    E inicio o reparo
    Então a execução deve estar no estado "IN_REPAIR"
    E concluo a execução
    Então a execução deve estar no estado "COMPLETED"
    E o histórico deve conter as transições "QUEUED,IN_DIAGNOSIS,IN_REPAIR,COMPLETED"
    E deve existir exatamente um evento outbox do tipo "execution.completed"
