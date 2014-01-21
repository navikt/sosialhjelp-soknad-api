angular.module('nav.markup.navinfoboks', [])
	.directive('navinfoboks', [function () {
		return {
			restrict   : 'A',
			replace    : true,
			transclude : true,
			templateUrl: '../js/app/directives/markup/navinfoboksTemplate.html'
		}
	}])
	.directive('vedlegginfoboks', [function () {
		return {
			restrict   : 'A',
			replace    : true,
			transclude : true,
			templateUrl: '../js/app/directives/markup/vedlegginfoboksTemplate.html'
		}
	}]);
