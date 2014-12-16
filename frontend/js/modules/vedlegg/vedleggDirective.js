angular.module('nav.vedlegg.directive', [])
    .directive('bildeNavigering', function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        };
    })
    .directive('vedleggTextInput', function(cms, $timeout) {
        return {
            scope: {
                forventning: '='
            },
            templateUrl: '../js/modules/vedlegg/template/vedleggTextInputTemplate.html',
            link: {
                pre: function(scope, element, attr) {
                    scope.nokkel = attr.nokkel;
                    scope.sporsmal = attr.nokkel + '.sporsmal';
                    scope.hjelpetekst = { tittel: attr.nokkel + '.hjelpetekst.tittel', tekst: attr.nokkel + ".hjelpetekst.tekst" };
                    scope.label = attr.nokkel + '.label';
                    scope.feilmelding = attr.nokkel + '.feilmelding';
                    scope.tellertekst = attr.nokkel + '.tellertekst';
                    scope.negativtellertekst = attr.nokkel + '.negativtellertekst';
                    scope.maxlengde = attr.maxlengde;
                    scope.counter = attr.maxlengde;
                },

                post: function(scope, element) {
                    scope.feil = false;
                    scope.harIkkeFeil = true;
                    var harFokus = false;

                    scope.lagreFaktum = function () {
                        scope.forventning.$save();
                    }

                    element.find('textarea').bind('focus', function () {
                        $timeout(function() {
                            harFokus = true;
                        });
                    });

                    element.find('textarea').bind('blur', function () {
                        $timeout(function() {
                            harFokus = false;
                        });
                    });

                    scope.harSporsmal = function() {
                        var tekst = cms.tekster[scope.sporsmal];
                        return isNotNullOrUndefined(tekst) && tekst.length > 0;
                    };

                    scope.harHjelpetekst = function() {
                        var tekst = cms.tekster[scope.hjelpetekst.tekst];
                        return isNotNullOrUndefined(tekst) && tekst.length > 0;
                    };

                    scope.harFokusOgFeil = function () {
                        return harFokus || scope.feil;
                    };
                }
            }
        }
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