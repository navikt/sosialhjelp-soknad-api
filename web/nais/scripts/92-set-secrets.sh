echo "Export apikeys"
export SOKNADSOSIALHJELP_SERVER_TPSWS_API_V1_APIKEY_PASSWORD=$(cat /apigw/tpswsapi/x-nav-apiKey)
export SOKNADSOSIALHJELP_SERVER_NORG2_API_V1_APIKEY_PASSWORD=$(cat /apigw/norg2api/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_HUSBANKEN_BOSTOTTE_APIKEY_PASSWORD=$(cat /apigw/husbankenapi/x-nav-apiKey)
export SOKNADSOSIALHJELP_SERVER_SKATT_INNTEKTSMOTTAKER_APIKEY_PASSWORD=$(cat /apigw/skatt_inntektsmottakerapi/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_EREGAPI_APIKEY_PASSWORD=$(cat /apigw/eregapi/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_AAREGAPI_APIKEY_PASSWORD=$(cat /apigw/aaregapi/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_STSTOKEN_APIKEY_PASSWORD=$(cat /apigw/securitytokenservicetoken/x-nav-apiKey)
export SOSIALHJELP_SOKNAD_API_DKIFAPI_APIKEY_PASSWORD=$(cat /apigw/dkifapi/x-nav-apiKey)

echo "Export serviceuser credentials"
export SRVSOKNADSOSIALHJELP_SERVER_USERNAME=$(cat /serviceuser/srvsoknadsosialhjelp-server/username)
export SRVSOKNADSOSIALHJELP_SERVER_PASSWORD=$(cat /serviceuser/srvsoknadsosialhjelp-server/password)

export SRVSOKNADSOSIALHJELP_SERVER_SBS_USERNAME=$(cat /serviceuser/srvsoknadsosialhjelp-server-sbs/username)
export SRVSOKNADSOSIALHJELP_SERVER_SBS_PASSWORD=$(cat /serviceuser/srvsoknadsosialhjelp-server-sbs/password)
