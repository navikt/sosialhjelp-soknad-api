angular.module('nav.barnetillegg.directive', [])
    .directive('barnetilleggScrollDirective', ['$cookieStore', '$timeout', function ($cookieStore, $timeout) {
        return function ($scope) {
            $timeout(function () {
                var barneCookie = $cookieStore.get('barneCookie');
                if (barneCookie) {
                    $scope.$emit('CLOSE_TAB', 'reell-arbeidssoker');
                    $scope.$emit('OPEN_TAB', barneCookie.aapneTabs);

                    var faktumId = barneCookie.barneFaktumId;
                    var element;
                    if (faktumId) {
                        element = angular.element('#barn' + faktumId);
                    } else {
                        element = angular.element('#barnetillegg');
                    }

                    $timeout(
                        function () {
                            scrollToElement(element, 0);
                            fadeBakgrunnsfarge(element.parent(), $scope, 255, 255, 255);
                        }
                        , 600);
                    $cookieStore.remove('barneCookie');
                }
            })
        }
    }]);
