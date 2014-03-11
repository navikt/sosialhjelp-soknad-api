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
    .filter('cmstekst', ['cms', '$rootScope', function(cms, $rootScope) {
        return function(nokkel) {
            var tekst = cms.tekster[nokkel];

            if ($rootScope.visCmsnokkler) {
                tekst += ' [' + nokkel + ']';
            }

            return tekst === undefined ? '' : tekst;
        };
    }]);
