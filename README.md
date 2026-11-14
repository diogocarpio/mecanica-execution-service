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

### Docker Compose

Copie o contrato de variáveis e suba o ambiente completo:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

O Compose sobe a aplicação, MongoDB e RabbitMQ na mesma rede Docker. A aplicação
fica disponível em `http://localhost:8080`, o painel do RabbitMQ em
`http://localhost:15672` e o broker AMQP em `localhost:5672`. As credenciais
locais padrão são `execution` / `execution`; altere o arquivo `.env` quando
necessário.

O RabbitMQ é habilitado com `BROKER_ENABLED=true`. Na inicialização, o próprio
Spring AMQP declara a exchange durável `os.exchange` do tipo `topic`, as filas e
os bindings usados pelos listeners:

| Fila | Routing key | Função |
|---|---|---|
| `execution.os.approved.queue` | `os.approved` | Autoriza a execução da OS |
| `execution.os.cancelled.queue` | `os.cancelled` | Processa o cancelamento da OS |

As filas de consumo usam a exchange de dead letter `os.exchange.dlx` e suas
respectivas DLQs (`execution.os.approved.dlq` e
`execution.os.cancelled.dlq`). A aplicação também declara as filas de
publicação previstas em `MessagingConfig`, mas elas não são listeners de
entrada.

Para acompanhar os serviços ou desligar o ambiente:

```powershell
docker compose ps
docker compose logs -f app
docker compose down
docker compose down -v # remove também os dados locais
```

O `.env` é ignorado pelo Git e deve conter somente valores locais; não use essas
credenciais padrão em produção.

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
