angular.module('nav.bildenavigering', [])
    .directive('bildenavigering', [function () {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                vedlegg: '='
            },

            link: function (scope, element) {
                var bilder = [];
                scope.side = 0;
                scope.bilder = bilder;

                scope.range = function (til) {
                    var r = [];
                    for (var i = 0; i < til; i++) {
                        r.push(i);
                    }
                    return r;
                };

                scope.sideErSynlig = function (index) {
                    return index === scope.side;
                };

                scope.naviger = function (retning) {
                    scope.side = scope.side + retning;
                    if (scope.side < 0) {
                        scope.side = scope.vedlegg.antallSider - 1;
                    } else if (scope.side >= scope.vedlegg.antallSider) {
                        scope.side = 0;
                    }
                };

                scope.hentTimestamp = new Date().getTime();
            },

            templateUrl: function (element, attr) {
                if (attr.selvstendig) {
                    return '../js/dagpenger/directives/bildenavigering/bildenavigeringTemplateStor.html';
                } else {
                    return '../js/dagpenger/directives/bildenavigering/bildenavigeringTemplateLiten.html';
                }
            }
        };
    }]);

