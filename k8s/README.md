# Kubernetes

## Pré-requisitos

- Cluster Kubernetes gerenciado com `kubectl` e Kustomize.
- `metrics-server` instalado e funcional para o HPA.
- A imagem da aplicação publicada em um registry acessível pelo cluster.
- Os Secrets `execution-database` e `execution-rabbitmq` criados por pipeline
  ou processo externo.

## Configuração da imagem

O pipeline aplica `k8s/overlays/staging` ou `k8s/overlays/production` e substitui
`mecanica-execution-service:0.0.0` pela imagem imutável publicada no ECR. Não
use `latest`.

## Secrets obrigatórios

| Secret | Chaves |
| --- | --- |
| `execution-database` | `uri` |
| `execution-rabbitmq` | `host`, `username`, `password` |

`secret-contract.yaml` é apenas documentação do contrato. Ele não está incluído
no `kustomization.yaml` e não deve receber valores reais no Git.

## Deploy e rollback

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -k k8s/
kubectl -n mecanica-execution rollout status deployment/mecanica-execution-service
kubectl -n mecanica-execution get hpa,pods
kubectl -n mecanica-execution rollout undo deployment/mecanica-execution-service
```

O `NetworkPolicy` limita a entrada ao próprio namespace. Antes de ativá-lo em
um cluster com ingress, observabilidade ou políticas globais, valide as regras
necessárias para não bloquear tráfego legítimo.

Os ambientes do pipeline usam os namespaces `mecanica-execution-staging` e
`mecanica-execution-production`, ambos com o label
`app.kubernetes.io/part-of=mecanica`. O pipeline lê um JSON do AWS Secrets
Manager com as chaves `MONGODB_URI`, `RABBITMQ_HOST`, `RABBITMQ_USERNAME` e
`RABBITMQ_PASSWORD`, e cria os Secrets Kubernetes sem versioná-los.
