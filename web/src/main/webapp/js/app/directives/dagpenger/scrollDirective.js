angular.module('nav.scroll.directive', [])
	.directive('scrollTilbakeDirective', [function () {
		return {
			replace   : false,
			controller: ['$scope', '$timeout', '$cookieStore', function ($scope, $timeout, $cookieStore) {
				var cookiename = 'scrollTil';

				$timeout(function () {
					var cookie = $cookieStore.get(cookiename);
					if (cookie) {
                        $scope.apneTab(cookie.aapneTabs);

                        $timeout(function() {
                            var faktumId = cookie.faktumId;
                            var blokkelement = angular.element('#'+cookie.aapneTabs);

                            // TODO Burde være etter scrolling siden denne får browseren til å scrolle selv
                            var fokusElement = $(blokkelement).find('.knapp-leggtil-liten').first().focus();
                            var scrollElement;
                            if (faktumId) {
                                scrollElement = $(blokkelement).find('#' + cookie.aapneTabs + faktumId);
                            } else {
                                scrollElement = fokusElement;
                            }

                            $timeout(function () {
                                scrollToElement(scrollElement, 200);
                                fokusElement.focus();
                                fadeBakgrunnsfarge(scrollElement.parent(), $scope, 255, 255, 255);
                            }, 600);
                            $cookieStore.remove(cookiename);
                        });
					}
				});
			}]
		}
	}]);
