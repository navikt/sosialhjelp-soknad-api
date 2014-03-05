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
    .directive('cmshtml', ['cms', function (cms) {
        return function ($scope, element, attrs) {
            var nokkel = attrs.cmshtml;
            element.html(cms.tekster[nokkel]);
        };
    }])
    .filter('cmstekst', ['cms', function(cms) {
        return function(nokkel) {
            var tekst = cms.tekster[nokkel];

            if ($rootScope.visCmsnokkler) {
                tekst += ' [' + nokkel + ']';
            }

            return tekst === undefined ? '' : tekst;
        };
    }]);
