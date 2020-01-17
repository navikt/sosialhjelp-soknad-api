echo "Export apikeys for apigw"
export SOSIALHJELP_SOKNAD_API_EREGAPI_APIKEY_PASSWORD=$(cat /apigw/eregapi/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD=$(cat /apigw/aaregapi/x-nav-apiKey)
