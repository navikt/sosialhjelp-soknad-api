angular.module('nav.cms.filter', [])
    .filter('configUrl', function (data) {
        return function (nokkel) {
            var url = data.config[nokkel.toLowerCase() + '.url'];

            return url === undefined ? '' : url;
        };
    })
    .filter('cmstekst', function (cmsService) {
        /**
         * nokkel: Kan være objekt eller string. Dersom string, så er det nøkkelen som blir brukt i CMS.
         * Dersom det er ett objekt, så skal objektet inneholde to nøkler, en 'key' og en 'fallbackKey', begge som string.
         * key inneholder nøkkel for teksten i CMS. Dersom key ikke finnes, brukers fallbackKey.
         *
         * args: Ett array eller en string. Dersom det er en string, settes denne verdien inn i CMS-teksten for index 0
         * Dersom ett array, settes teksten ved en index inn i tilsvarende posisjon i CMS-teksten. Posisjon i CMS-teksten
         * markeres ved '{0}', '{1}' osv
         */
        return function (nokkel, args) {
            return cmsService.getText(nokkel, args)
        };
    });