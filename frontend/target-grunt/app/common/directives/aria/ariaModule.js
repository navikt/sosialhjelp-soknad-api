angular.module('nav.aria', [])
	.directive('navAriaHidden', [function () {
		return {
			link: function (scope, element, attrs) {
				scope.$watch(
					function () {
						return scope.$eval(attrs.navAriaHidden);
					},
					function (nyVerdi, gammelVerdi) {
						if (nyVerdi == gammelVerdi) {
							return;
						}
						element.attr('aria-hidden', nyVerdi);
					}
				);
			}
		};
	}])
	.directive('navAriaExpanded', [function () {
		return {
			link: function (scope, element, attrs) {
				scope.$watch(
					function () {
						return scope.$eval(attrs.navAriaExpanded);
					},
					function (nyVerdi, gammelVerdi) {
						if (nyVerdi == gammelVerdi) {
							return;
						}
						element.attr('aria-expanded', nyVerdi);
					}
				);
			}
		};
	}]);
