echo "Export apikeys"
export AAREGAPI_APIKEY=$(cat /apigw/aaregapi/x-nav-apiKey)
export STSTOKEN_APIKEY=$(cat /apigw/securitytokenservicetoken/x-nav-apiKey)
export PDLAPI_APIKEY=$(cat /apigw/pdlapi/x-nav-apiKey)
