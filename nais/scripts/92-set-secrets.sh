echo "Export apikeys"
export AAREGAPI_APIKEY=$(cat /apigw/aaregapi/x-nav-apiKey)
export STSTOKEN_APIKEY=$(cat /apigw/securitytokenservicetoken/x-nav-apiKey)
export DKIFAPI_APIKEY=$(cat /apigw/dkifapi/x-nav-apiKey)
export PDLAPI_APIKEY=$(cat /apigw/pdlapi/x-nav-apiKey)
export KODEVERKAPI_APIKEY=$(cat /apigw/kodeverkapi/x-nav-apiKey)
export OPPSLAGAPI_APIKEY=$(cat /apigw/sosialhjelp_oppslag_api/x-nav-apiKey)

echo "Export serviceuser credentials"
export SRVSOKNADSOSIALHJELP_SERVER_USERNAME=$(cat /serviceuser/srvsoknadsosialhjelp-server/username)
export SRVSOKNADSOSIALHJELP_SERVER_PASSWORD=$(cat /serviceuser/srvsoknadsosialhjelp-server/password)

export SRVSOKNADSOSIALHJELP_SERVER_SBS_USERNAME=$(cat /serviceuser/srvsoknadsosialhjelp-server-sbs/username)
export SRVSOKNADSOSIALHJELP_SERVER_SBS_PASSWORD=$(cat /serviceuser/srvsoknadsosialhjelp-server-sbs/password)
