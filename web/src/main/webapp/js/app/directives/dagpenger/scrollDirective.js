angular.module('nav.scroll.directive', [])
    .directive('scrollTilbakeDirective', [function () {
        return {
            replace: false,
            scope: true,
            controller: ['$scope', '$attrs', '$timeout', '$cookieStore', function ($scope, $attrs, $timeout, $cookieStore) {
                var cookiename = $attrs.scrollTilbakeDirective;

                $timeout(function () {
                    var cookie = $cookieStore.get(cookiename);
                    if (cookie) {
                        $scope.$emit("CLOSE_TAB", "reell-arbeidssoker");
                        $scope.$emit("OPEN_TAB", cookie.aapneTabs);

                        var faktumId = cookie.faktumId;
                        var element;
                        if (faktumId) {
                            element = angular.element("#"+cookiename + faktumId);
                        } else {
                            element = angular.element("#"+cookiename);
                        }

                        $timeout(
                            function () {
                                scrollToElement(element, 0);
                                fadeBakgrunnsfarge(element.parent(), $scope, 255, 255, 255);
                            }
                            , 600);
                        $cookieStore.remove(cookiename);
                    }
                })
                
            }]
        }
    }])