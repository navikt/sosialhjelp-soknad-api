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
    .filter('configUrl', ['data', function(data) {
        return function(nokkel) {
            var url = data.config[nokkel.toLowerCase() + '.url'];

            return url === undefined ? '' : url;
        };
    }])
    .filter('cmstekst', ['cms', function(cms) {
        return function(nokkel) {
            var tekst = cms.tekster[nokkel];

            return tekst === undefined ? '' : tekst;
        };
    }]);
