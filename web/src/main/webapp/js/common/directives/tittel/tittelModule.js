angular.module('nav.skjematittel', [])
	.directive('navTittel', [function () {
		return {
			replace    : true,
			link       : {
				pre: function (scope, element, attrs) {
					scope.tittel = attrs.navTittel;
				}
			},
			templateUrl: '../js/common/directives/tittel/tittelTemplate.html'
		}
	}]);
