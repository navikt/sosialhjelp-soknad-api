angular.module('nav.vedlegg.directive', [])
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        };
    }])
    .directive('fjernRadioValg', function() {
        return {
            scope:  {
                forventning: '=fjernRadioValg'
            },
            link: function(scope, element, attrs) {
                var forrigeValg = scope.forventning.innsendingsvalg;
                var valg = attrs.value;
                element.bind('click', function() {
                    if (scope.forventning.innsendingsvalg === forrigeValg) {
                        scope.forventning.innsendingsvalg = 'VedleggKreves';
                        scope.$parent.endreInnsendingsvalg('', true);
                        scope.forventning.$save();
                    } else {
                        scope.forventning.innsendingsvalg = valg;
                        scope.forventning.$save();
                        scope.$parent.endreInnsendingsvalg(true, false);
                    }
                    forrigeValg = scope.forventning.innsendingsvalg;
                    scope.$apply();
                });
            }
        };
    });