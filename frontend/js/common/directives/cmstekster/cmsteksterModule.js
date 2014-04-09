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
        return function(nokkel, args) {
            var tekst = cms.tekster[nokkel];

            if (args instanceof Array) {
                args.forEach(function(argTekst, idx) {
                    tekst = tekst.replace('{' + idx + '}', argTekst);
                });
            } else if (args) {
                tekst = tekst.replace('{0}', args);
            }

            if ($rootScope.visCmsnokkler) {
                tekst += ' [' + nokkel + ']';
            }

            return tekst === undefined ? '' : $sce.trustAsHtml(tekst);
        };
    }]);
