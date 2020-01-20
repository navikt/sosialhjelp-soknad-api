echo "Export apikeys for apigw"
export SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD=$(cat /apigw/tpswsapi/x-nav-apiKey)
export SOKNADSOSIALHJELP_SERVER_NORG2_API_V1_APIKEY_PASSWORD=$(cat /apigw/norg2api/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_HUSBANKEN_BOSTOTTE_APIKEY_PASSWORD=$(cat /apigw/husbankenapi/x-nav-apiKey)