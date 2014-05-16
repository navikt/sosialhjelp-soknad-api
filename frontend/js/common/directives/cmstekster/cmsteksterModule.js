angular.module('nav.cmstekster', [])
	.directive('cmsvedlegg', [function () {
		return {
			scope   : false,
			required: 'navFaktum',
			link    : {
				pre: function (scope, elem, attr) {
                    scope.cmsProps = {};
                    if (attr.cmsvedlegg) {
						scope.cmsProps.ekstra = attr.cmsvedlegg;
					}
                }
			}
		};
	}])
    .filter('configUrl', ['data', function(data) {
        return function(nokkel) {
            var url = data.config[nokkel.toLowerCase() + '.url'];

            return url === undefined ? '' : url;
        };
    }])
    .filter('cmstekst', ['cms', '$sce', '$rootScope', function(cms, $sce, $rootScope) {
        /**
         * nokkel: Kan være objekt eller string. Dersom string, så er det nøkkelen som blir brukt i CMS.
         * Dersom det er ett objekt, så skal objektet inneholde to nøkler, en 'key' og en 'fallbackKey', begge som string.
         * key inneholder nøkkel for teksten i CMS. Dersom key ikke finnes, brukers fallbackKey.
         *
         * args: Ett array eller en string. Dersom det er en string, settes denne verdien inn i CMS-teksten for index 0
         * Dersom ett array, settes teksten ved en index inn i tilsvarende posisjon i CMS-teksten. Posisjon i CMS-teksten
         * markeres ved '{0}', '{1}' osv
         */
        return function(nokkel, args) {
            var tekst, usedKey = nokkel;

            if (nokkel instanceof Object) {
                tekst = cms.tekster[nokkel.key];
                usedKey = nokkel.key;
                if (tekst === undefined) {
                    tekst = cms.tekster[nokkel.fallbackKey];
                    usedKey = nokkel.fallbackKey;
                }
            } else {
                tekst = cms.tekster[nokkel];
            }

            if (args instanceof Array) {
                args.forEach(function(argTekst, idx) {
                    tekst = tekst.replace('{' + idx + '}', argTekst);
                });
            } else if (args) {
                tekst = tekst.replace('{0}', args);
            }

            if ($rootScope.visCmsnokkler) {
                tekst += ' [' + usedKey + ']';
            }

            return tekst === undefined ? '' : $sce.trustAsHtml(tekst);
        };
    }]);
