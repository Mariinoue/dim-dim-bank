#!/bin/bash
set -euo pipefail

RM="${RM:?Defina a variavel RM. Ex.: export RM=rm999999}"
LOCATION="${LOCATION:-brazilsouth}"
SQL_LOCATION="${SQL_LOCATION:-$LOCATION}"

RESOURCE_GROUP_NAME="rg-dimdim"
APP_SERVICE_PLAN="plan-dimdim"
WEBAPP_NAME="dimdim-web-${RM}"
APP_INSIGHTS_NAME="ai-dimdim"
RUNTIME="JAVA:17-java17"

SQL_SERVER_NAME="dimdim-sql-${RM}"
SQL_DB_NAME="dimdimdb"
SQL_ADMIN_USER="dimdimadmin"

if [ -z "${SQL_ADMIN_PASSWORD:-}" ]; then
  read -r -s -p "Senha do administrador do SQL (min. 8 caracteres, com maiuscula, minuscula, numero e simbolo): " SQL_ADMIN_PASSWORD
  echo
fi

echo ">> Registrando providers"
az provider register --namespace Microsoft.Web
az provider register --namespace Microsoft.Insights
az provider register --namespace Microsoft.OperationalInsights
az provider register --namespace Microsoft.ServiceLinker
az provider register --namespace Microsoft.Sql
az extension add --name application-insights --only-show-errors

echo ">> Criando o Grupo de Recursos"
az group create --name "$RESOURCE_GROUP_NAME" --location "$LOCATION"

echo ">> Criando o Application Insights"
az monitor app-insights component create \
  --app "$APP_INSIGHTS_NAME" \
  --location "$LOCATION" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --application-type web

echo ">> Criando o servidor Azure SQL"
az sql server create \
  --name "$SQL_SERVER_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --location "$SQL_LOCATION" \
  --admin-user "$SQL_ADMIN_USER" \
  --admin-password "$SQL_ADMIN_PASSWORD"

echo ">> Criando o banco Azure SQL Database"
az sql db create \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --server "$SQL_SERVER_NAME" \
  --name "$SQL_DB_NAME" \
  --edition Basic \
  --capacity 5

echo ">> Liberando o acesso dos servicos do Azure (Web App) ao servidor SQL"
az sql server firewall-rule create \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --server "$SQL_SERVER_NAME" \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

echo ">> Criando o Plano de Servico e o Web App"
az appservice plan create \
  --name "$APP_SERVICE_PLAN" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --location "$LOCATION" \
  --sku F1 \
  --is-linux

az webapp create \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --plan "$APP_SERVICE_PLAN" \
  --runtime "$RUNTIME"

echo ">> Habilitando a autenticacao basica (SCM) para o deploy"
az resource update \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --namespace Microsoft.Web \
  --resource-type basicPublishingCredentialsPolicies \
  --name scm \
  --parent "sites/$WEBAPP_NAME" \
  --set properties.allow=true

echo ">> Configurando variaveis de ambiente (banco + Application Insights)"
CONNECTION_STRING=$(az monitor app-insights component show \
  --app "$APP_INSIGHTS_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --query connectionString \
  --output tsv)

JDBC_URL="jdbc:sqlserver://${SQL_SERVER_NAME}.database.windows.net:1433;database=${SQL_DB_NAME};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

az webapp config appsettings set \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME" \
  --settings \
    APPLICATIONINSIGHTS_CONNECTION_STRING="$CONNECTION_STRING" \
    ApplicationInsightsAgent_EXTENSION_VERSION="~3" \
    XDT_MicrosoftApplicationInsights_Mode="Recommended" \
    XDT_MicrosoftApplicationInsights_PreemptSdk="1" \
    SPRING_DATASOURCE_URL="$JDBC_URL" \
    SPRING_DATASOURCE_USERNAME="$SQL_ADMIN_USER" \
    SPRING_DATASOURCE_PASSWORD="$SQL_ADMIN_PASSWORD" \
  --output none

az webapp restart --name "$WEBAPP_NAME" --resource-group "$RESOURCE_GROUP_NAME"

echo ">> Conectando o Web App ao Application Insights"
az monitor app-insights component connect-webapp \
  --app "$APP_INSIGHTS_NAME" \
  --web-app "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP_NAME"

echo
echo "Pronto!"
echo "  Web App : https://${WEBAPP_NAME}.azurewebsites.net"
echo "  SQL     : ${SQL_SERVER_NAME}.database.windows.net / ${SQL_DB_NAME}"
echo "Proximo passo: execute scripts/ddl.sql no banco e faca o deploy (02 ou 03)."
