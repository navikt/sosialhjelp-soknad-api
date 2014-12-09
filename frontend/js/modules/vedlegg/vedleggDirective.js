angular.module('nav.vedlegg.directive', [])
    .directive('bildeNavigering', function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        };
    })
    .directive('fjernRadioValg', function() {
        return {
            scope:  {
                forventning: '=fjernRadioValg'
            },
            link: function(scope, element, attrs) {
                function skalFjerneAvhukingen() {
                    return scope.forventning.innsendingsvalg === scope.forventning.forrigeinnsendingsvalg;
                }

                function settInnsendingsvalg(nyttValg, hiddenValue, skalViseFeil) {
                    scope.forventning.innsendingsvalg = nyttValg;
                    scope.$parent.endreInnsendingsvalg(hiddenValue, skalViseFeil);
                    scope.forventning.forrigeinnsendingsvalg = scope.forventning.innsendingsvalg;
                    scope.forventning.$save();
                    scope.$apply();
                }

                var valg = attrs.value;
                element.bind('click', function() {
                    if (skalFjerneAvhukingen()) {
                        settInnsendingsvalg("VedleggKreves", '', true);
                    } else {
                        settInnsendingsvalg(valg, true, false);
                    }
                });
            }
        };
    });