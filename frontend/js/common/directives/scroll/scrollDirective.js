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
                            var blokkelement = angular.element(cookie.gjeldendeTab);

                            var fokusElement = $(blokkelement).find('.knapp-leggtil-liten').first();

                            $timeout(function () {
                                var scrollElement;
                                if (faktumId) {
                                    scrollElement = $(blokkelement).find(cookie.gjeldendeTab + faktumId);
                                } else {
                                    scrollElement = fokusElement;
                                }
                                scrollToElement(scrollElement, 200);
                                $timeout(function() {
                                    fokusElement.focus();
                                }, 200);
                                if(!(scrollElement.parent().hasClass("ikke-fadebakgrunn"))) {
                                    fadeBakgrunnsfarge(scrollElement.parent(), $scope, 241, 241, 241);
                                }
                            }, 600);
                            $cookieStore.remove(cookiename);
                        });
					}
				});
			}]
		};
	}])
    .directive('scrollTilElement', [function() {
        return function(scope, element, attrs) {
            var selector = attrs.scrollTilElement;
            var offset = attrs.offset !== undefined ? parseInt(attrs.offset) : 0;

            element.bind('click', function() {
                scrollToElement($(selector), offset);
            });
        }
    }]);
