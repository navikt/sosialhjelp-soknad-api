angular.module('nav.cmstekster', [])
    .directive('cmsvedlegg', function () {
        return {
            scope: false,
            required: 'navFaktum',
            link: {
                pre: function (scope, elem, attr) {
                    scope.cmsProps = {};
                    if (attr.cmsvedlegg) {
                        scope.cmsProps.ekstra = attr.cmsvedlegg;
                    }
                }
            }
        };
    })
    .filter('configUrl', function (data) {
        return function (nokkel) {
            var url = data.config[nokkel.toLowerCase() + '.url'];

            return url === undefined ? '' : url;
        };
    })
    .filter('cmstekst', function (cms, $sce, $rootScope, data, cmsprefix, $injector) {
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
            try {

            }
            $injector('cmsprefix')

            var tekst;
            var key = getKeyToUse();
            tekst = cms.tekster[key];

            if (args instanceof Array) {
                args.forEach(function (argTekst, idx) {
                    tekst = tekst.replace('{' + idx + '}', argTekst);
                });
            } else if (args) {
                tekst = tekst.replace('{0}', args);
            }

            if ($rootScope.visCmsnokkler) {
                tekst += ' [' + key + ']';
            }

            return tekst === undefined ? '' : $sce.trustAsHtml(tekst);

            function getKeyToUse() {
                if (nokkel instanceof Object) {
                    if (cms.tekster[prefix + nokkel.key]) {
                        return cmsprefix + nokkel.key;
                    } else if (cms.tekster[nokkel.key]) {
                        return nokkel.key;
                    } else if (cms.tekster[cmsprefix + nokkel.fallbackKey]) {
                        return cms.tekster[cmsprefix + nokkel.fallbackKey];
                    } else {
                        return nokkel.fallbackKey;
                    }
                } else {
                    if (cms.tekster[cmsprefix + nokkel]) {
                        return cmsprefix + nokkel;
                    } else {
                        return nokkel;
                    }
                }
            }
        };
    });
