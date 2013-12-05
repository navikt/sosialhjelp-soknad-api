angular.module('nav.bildenavigering', [])
    .directive('bildenavigering', [function () {
        return {
            restrict: "A",
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
                    for (var i = 0; i < til; i++) r.push(i);
                    return r;
                }
                scope.sideErSynlig = function (index) {
                    console.log("synlig" + index + ":" + scope.side + ": " + (index == scope.side))
                    return index == scope.side;
                }
                scope.naviger = function (retning) {
                    //element.find('img').attr('src', '');
                    scope.side = scope.side + retning;
                    if (scope.side < 0) {
                        scope.side = scope.vedlegg.antallSider - 1;
                    } else if (scope.side >= scope.vedlegg.antallSider) {
                        scope.side = 0;
                    }
                }

            },
            templateUrl: function (element, attr) {
                console.log('\'' + attr.selvstendig + '\'' + (attr.selvstendig)?'ja':'nei')
                if (attr.selvstendig) {
                    console.log('stor')
                    return '../js/app/directives/bildenavigering/bildenavigeringTemplateStor.html';
                } else {
                    console.log('liten')
                    return '../js/app/directives/bildenavigering/bildenavigeringTemplateLiten.html';
                }
            }
        }
    }]);
