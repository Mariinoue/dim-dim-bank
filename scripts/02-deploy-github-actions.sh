#!/bin/bash
set -euo pipefail

RM="${RM:?Defina a variavel RM. Ex.: export RM=rm999999}"
GITHUB_REPO_NAME="${GITHUB_REPO_NAME:?Defina GITHUB_REPO_NAME. Ex.: export GITHUB_REPO_NAME=usuario/dimdim-webapp}"
BRANCH="${BRANCH:-main}"

RESOURCE_GROUP_NAME="rg-dimdim"
WEBAPP_NAME="dimdim-web-${RM}"

az webapp deployment github-actions add \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --repo "$GITHUB_REPO_NAME" \
  --branch "$BRANCH" \
  --login-with-github

echo
echo "Workflow criado no GitHub. Acompanhe em: https://github.com/${GITHUB_REPO_NAME}/actions"
echo "Os testes usam H2 em memoria, entao o build nao precisa de segredos do banco."
