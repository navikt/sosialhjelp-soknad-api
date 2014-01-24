angular.module('nav.scroll.directive', [])
	.directive('scrollTilbakeDirective', [function () {
		return {
			replace   : false,
			scope     : true,
			controller: ['$scope', '$attrs', '$timeout', '$cookieStore', function ($scope, $attrs, $timeout, $cookieStore) {
				var cookiename = $attrs.scrollTilbakeDirective;

				$timeout(function () {
					var cookie = $cookieStore.get(cookiename);
					if (cookie) {
						$scope.$emit('CLOSE_TAB', 'reell-arbeidssoker');
						$scope.$emit('OPEN_TAB', cookie.aapneTabs);

						var faktumId = cookie.faktumId;
						var blokkelement = angular.element('#'+cookiename);
                        var fokusElement = $(blokkelement).find('.knapp-leggtil-liten').first().focus();
                        var scrollElement;
						if (faktumId) {
                            scrollElement = $(blokkelement).find('#' + cookiename + faktumId);
						} else {
                            scrollElement = fokusElement;
                        }

						$timeout(function () {
							scrollToElement(scrollElement, 200);
                            fokusElement.focus();
							fadeBakgrunnsfarge(scrollElement.parent(), $scope, 255, 255, 255);
						}, 600);
						$cookieStore.remove(cookiename);
					}
				});
			}]
		}
	}]);
