angular.module('nav.markup.navinfoboks', [])
    .directive('navinfoboks', function ($parse) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: '../js/common/directives/markup/navinfoboksTemplate.html',
            link: function (scope, el, attrs) {
                var infotekster = $parse(attrs.infotekster)(scope);
                if (typeof infotekster === 'string') {
                    scope.infoTekster = [infotekster];
                } else {
                    scope.infoTekster = infotekster;
                }
            }
        };
    })
    .directive('vedlegginfoboks', function ($parse) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: '../js/common/directives/markup/vedlegginfoboksTemplate.html',
            link: function (scope, el, attrs) {
                var vedleggtekster = $parse(attrs.vedleggtekster)(scope);
                if (typeof vedleggtekster === 'string') {
                    scope.vedleggTekster = [vedleggtekster];
                } else {
                    scope.vedleggTekster = vedleggtekster;
                }
            }
        };
    });
