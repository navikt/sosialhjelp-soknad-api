angular.module('nav.markup.navinfoboks', [])
	.directive('navinfoboks', function ($parse) {
		return {
			restrict   : 'A',
			replace    : true,
			templateUrl: '../js/common/directives/markup/navinfoboksTemplate.html',
            link: function(scope, el, attrs) {
                scope.infoTekster = $parse(attrs.infotekster)(scope);
            }
		};
	})
	.directive('vedlegginfoboks', function ($parse) {
		return {
			restrict   : 'A',
			replace    : true,
			templateUrl: '../js/common/directives/markup/vedlegginfoboksTemplate.html',
            link: function(scope, el, attrs) {
                scope.vedleggTekster = $parse(attrs.vedleggtekster)(scope);
            }
		};
	});
