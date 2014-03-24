angular.module('nav.markup.panelbelyst', [])
	.directive('panelbelyst', [function () {
		return {
			restrict   : 'A',
			replace    : true,
			transclude : true,
			templateUrl: '../js/common/directives/markup/panelStandardBelystTemplate.html'
		};
	}]);
