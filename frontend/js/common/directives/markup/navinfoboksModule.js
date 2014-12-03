angular.module('nav.markup.navinfoboks', [])
    .directive('navinfoboks', function ($parse) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: '../js/common/directives/markup/navinfoboksTemplate.html',
            link: function (scope, el, attrs) {
                var infotekster = $parse(attrs.infotekster)(scope);
                scope.infoTekster = [];
                typeof infotekster === 'string' ? scope.infoTekster.push(infotekster) : scope.infoTekster = infotekster;
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
                scope.vedleggTekster = [];
                typeof vedleggtekster === 'string' ? scope.vedleggTekster.push(vedleggtekster) : scope.vedleggTekster = vedleggtekster;
            }
        };
    });
