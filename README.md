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
.
├── .github/workflows/      # pipeline de testes, análise e deploy
├── docs/                   # arquitetura, contratos, migração e evidências
├── k8s/                    # namespace, manifests base e overlays
├── terraform/               # infraestrutura AWS, EKS, ECR e integração CI/CD
├── src/
│   ├── main/
│   │   ├── java/com/f1rsters/execution/
│   │   │   ├── config/         # configuração Spring, RabbitMQ e clock
│   │   │   ├── controller/     # endpoints HTTP e tratamento de erros
│   │   │   ├── domain/         # entidades, estados e regras de negócio
│   │   │   ├── dto/            # contratos de entrada, saída e eventos
│   │   │   ├── exception/      # exceções de domínio e contratos
│   │   │   ├── listener/       # consumidores RabbitMQ e publicadores outbox
│   │   │   ├── mapper/         # conversão domínio/DTO
│   │   │   ├── repository/      # repositórios MongoDB
│   │   │   ├── servide/         # casos de uso e publicação da aplicação
│   │   │   ├── util/            # utilitários compartilhados
│   │   │   └── validation/      # validação de eventos recebidos
│   │   └── resources/           # configuração da aplicação
│   └── test/
│       ├── java/                # testes unitários e de persistência
│       └── resources/           # configuração dos testes
├── Dockerfile               # imagem da aplicação
├── pom.xml                  # dependências e ciclo de build Maven
└── README.md
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

Para executar o fluxo BDD completo de autorização, diagnóstico, reparo e conclusão:

```powershell
.\mvnw.cmd -B -Dtest=CucumberTest test
```

## Kubernetes

Os manifests em `k8s/` são destinados a um cluster Kubernetes gerenciado. O
Deployment usa `mecanica-execution-service:0.0.0` como valor inicial; antes de
aplicar, substitua a imagem por uma tag imutável publicada pelo pipeline:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl -n mecanica-execution create secret generic execution-database `
  --from-literal=uri="$env:MONGODB_URI" --dry-run=client -o yaml | kubectl apply -f -
kubectl -n mecanica-execution create secret generic execution-rabbitmq `
  --from-literal=host="$env:RABBITMQ_HOST" `
  --from-literal=username="$env:RABBITMQ_USERNAME" `
  --from-literal=password="$env:RABBITMQ_PASSWORD" `
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k k8s/
kubectl -n mecanica-execution rollout status deployment/mecanica-execution-service
```

`k8s/secret-contract.yaml` documenta somente as chaves obrigatórias e não deve
ser aplicado sem substituir os placeholders. O cluster precisa disponibilizar
`metrics-server` para o HPA de CPU/memória. Para atualizar a imagem com
Kustomize, ajuste `k8s/kustomization.yaml` e execute `kubectl apply -k k8s/`, ou
aplique a tag imutável pelo pipeline; nunca use `latest` em produção.

O HPA mantém entre 2 e 10 réplicas, com alvos iniciais de 70% de CPU e 75% de
memória. Esses valores devem ser recalibrados com testes de carga e, para o
consumidor RabbitMQ, complementados futuramente por uma métrica de profundidade
da fila.

## Terraform

O diretório `terraform/` provisiona a fundação AWS completa para os ambientes
staging e production: VPC, EKS, ECR, Secrets Manager e IAM/OIDC do GitHub
Actions. MongoDB e RabbitMQ permanecem externos; seus endpoints e credenciais
devem ser preenchidos no segredo de runtime por um processo seguro.

Consulte `terraform/README.md` para o bootstrap do backend remoto, variáveis,
outputs e ordem de execução. O Terraform não substitui o deploy da aplicação:
o workflow continua publicando a imagem e aplicando os overlays Kustomize.

## CI/CD e secrets AWS

Pull requests executam testes, cobertura JaCoCo e análise SonarCloud. Pushes em
`main` publicam a imagem no ECR e fazem deploy em staging; o deploy de produção
usa o GitHub Environment `production`, com aprovação obrigatória.

Configure como Variables compartilhadas `AWS_REGION`, `ECR_REPOSITORY`,
`SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY`, o Secret `SONAR_TOKEN` e, nos
GitHub Environments `staging` e `production`, as Variables `AWS_ROLE_ARN`,
`EKS_CLUSTER_NAME` e `RUNTIME_SECRET_ID`. O job de publicação usa o Environment
`staging`, onde a role precisa permitir push no ECR; cada job de deploy usa a
role do próprio ambiente para atualizar o EKS e ler somente o secret de runtime
correspondente no Secrets Manager.

O secret de runtime deve ser um JSON com `MONGODB_URI`, `RABBITMQ_HOST`,
`RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` e `NEW_RELIC_LICENSE_KEY`. Não configure chaves AWS estáticas
no GitHub e não versione valores reais em `k8s/secret-contract.yaml`.

## Métricas New Relic

O agente Java do New Relic é carregado no container e o aspecto
`ServiceMetricsAspect` registra automaticamente chamadas, sucessos, erros e
duração de todos os métodos públicos anotados com `@Service`. A chave
`NEW_RELIC_LICENSE_KEY` deve ser fornecida pelo secret de runtime.

## Status

⚠️ **Persistência MongoDB e topologia RabbitMQ foram implementadas; cutover distribuído ainda depende da validação dos contratos.**
