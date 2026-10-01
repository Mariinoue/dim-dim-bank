#!/bin/bash
set -euo pipefail

RM="${RM:?Defina a variavel RM. Ex.: export RM=rm999999}"

RESOURCE_GROUP_NAME="rg-dimdim"
WEBAPP_NAME="dimdim-web-${RM}"

echo ">> Compilando o projeto"
mvn clean package -DskipTests

echo ">> Enviando o .jar para o Web App"
az webapp deploy \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --name "$WEBAPP_NAME" \
  --src-path target/dimdim-0.0.1-SNAPSHOT.jar \
  --type jar

echo "Deploy concluido: https://${WEBAPP_NAME}.azurewebsites.net"
