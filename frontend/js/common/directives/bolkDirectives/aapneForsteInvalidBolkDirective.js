angular.module('nav.aapneForsteInvalidBolkDirective', [])
	.directive('apneBolker', ['$timeout', '$cookieStore', function ($timeout, $cookieStore) {
		return {
			require: '^form',
			link   : function (scope) {
                var cookiename = 'scrollTil';
                var cookie = $cookieStore.get(cookiename);
                if (!cookie) {
                    $timeout(function() {
                        var forsteInvalidBolk = $('.accordion-group').not('.validert').first();

                        scope.apneTab(forsteInvalidBolk.attr('id'));
                        $timeout(function() {
                            var fokusElement = forsteInvalidBolk.find('input').first();

                            if (fokusElement.length > 0) {
                                scrollToElement(fokusElement, 400);
                            }
                        });
                    });
                }
			}
		};
	}]);
