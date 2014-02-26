angular.module('nav.stickyFeilmelding', [])

    .directive('stickyFeilmelding', [function () {
        return {
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function (scope, element, attrs, ctrl) {
                scope.feil = [];
                scope.feil.antallFeil = 0;
                scope.feil.skalViseStickyFeilmeldinger = false;
                scope.feil.navaerende = 0;

                var elem = element.next();
                var bolker = $('[data-accordion-group]');
                var skalDeaktivereForrigeKnapp = true;
                scope.skalDeaktivereNesteKnapp = false;
                var feilHarBlittRettet = false;
                var nestForsteOgForsteRettet = false;
                var stodPaforsteRettetEnFeilEnFeilIgjen = false;

                scope.leggTilStickyFeilmelding = function () {
                    scope.feil.navaerende = 0;
                    feilHarBlittRettet = false;
                    nestForsteOgForsteRettet = false;
                    stodPaforsteRettetEnFeilEnFeilIgjen = false;

                    elem = element.next();
                    bolker = $('[data-accordion-group]');

                    scope.feil.skalViseStickyFeilmeldinger = true;
                    var elementerMedFeil = elem.find('.form-linje.feil, .form-linje.feilstyling');
                    scope.feil.antallFeil = elementerMedFeil.not('.ng-hide').length;

                    if (totalAntalLFeil() === 1) {
                        scope.skalDeaktivereNesteKnapp = true;
                    } else {
                        scope.skalDeaktivereNesteKnapp = false;
                    }
                };

                scope.forrige = function () {
                    if (sisteFeilAkkuratRettetOgFlereFeilIgjen() || enFeilIgjen() && !stodPaforsteRettetEnFeilEnFeilIgjen) {
                        scope.skalDeaktivereNesteKnapp = true;
                    } else {
                        scope.skalDeaktivereNesteKnapp = false;
                    }
                    if (!(feilHarBlittRettet && scope.feil.navaerende < 1)) {
                        if (scope.feil.navaerende > 0) {
                            leggTilMarkeringAvFeilmelding(-1, true);
                            feilHarBlittRettet = false;
                        } else if (scope.feil.navaerende === 0) {
                            if (stodPaforsteRettetEnFeilEnFeilIgjen) {
                                skalDeaktivereForrigeKnapp = true;
                            } else {
                                skalDeaktivereForrigeKnapp = true;
                                feilHarBlittRettet = false;
                                leggTilMarkeringAvFeilmelding(0, false);
                            }
                        }
                    }
                };

                scope.neste = function () {
                    stodPaforsteRettetEnFeilEnFeilIgjen = false;
                    if (!(feilHarBlittRettet && scope.feil.navaerende === totalAntalLFeil())) {
                        if (feilHarBlittRettet && scope.feil.navaerende > 0 && varIkkePaSisteFeil()) {
                            scope.feil.navaerende = scope.feil.navaerende - 1;
                            console.log("1");

                        }
                        if (varToFeilNaEnFeilStodPaAndreFeil()) {
                            leggTilMarkeringAvFeilmelding(1, true);
                            scope.skalDeaktivereNesteKnapp = true;
                            feilHarBlittRettet = false;
                            console.log("2");

                        } else if (varToFeilNaEnFeilStodPaForsteFeil()) {
                            leggTilMarkeringAvFeilmelding(0, true);
                            scope.skalDeaktivereNesteKnapp = true;
                            feilHarBlittRettet = false;
                            console.log("3");

                        } else if (feilHarBlittRettet && sisteFeilBleRettetOgStodPaNestSisteFeil()) {
                            scope.skalDeaktivereNesteKnapp = true;
                            scope.feil.navaerende = scope.feil.navaerende + 1;
                            feilHarBlittRettet = true;
                            console.log("4");

                        } else if (nestForsteOgForsteRettet) {
                            scope.feil.navaerende = 0;
                            leggTilMarkeringAvFeilmelding(1, true);
                            nestForsteOgForsteRettet = false;
                            feilHarBlittRettet = false;
                            console.log("5");

                        } else if (stodPaForsteFeilBleRettet()) {
                            leggTilMarkeringAvFeilmelding(0, true);
                            feilHarBlittRettet = false;
                            console.log("6");

                        } else if (varIkkePaSisteFeil()) {
                            leggTilMarkeringAvFeilmelding(1, true);
                            feilHarBlittRettet = false;
                            console.log("7");

                            if (erPaSisteFeil()) {
                                scope.skalDeaktivereNesteKnapp = true;
                            }
                        } else if (erPaSisteFeil()) {
                            leggTilMarkeringAvFeilmelding(0, false);
                            scope.skalDeaktivereNesteKnapp = true;
                            feilHarBlittRettet = false;
                            console.log("8");

                        } else if (sisteFeilBleRettetOgStodPaSisteFeil()) {
                            leggTilMarkeringAvFeilmelding(-1, true);
                            scope.skalDeaktivereNesteKnapp = true;
                            feilHarBlittRettet = false;
                            console.log("9");

                        }
                    } else if (feilHarBlittRettet && scope.feil.navaerende === 1 && totalAntalLFeil() === 1) {
                        leggTilMarkeringAvFeilmelding(-1, true);
                        console.log("10");
                    }
                };
                function sisteFeilAkkuratRettetOgFlereFeilIgjen() {
                    return scope.feil.navaerende === totalAntalLFeil() && feilHarBlittRettet;
                }

                function enFeilIgjen() {
                    return scope.feil.navaerende === 0 && totalAntalLFeil() === 1;
                }

                function varToFeilNaEnFeilStodPaAndreFeil() {
                    return feilHarBlittRettet && scope.feil.navaerende === -1 && totalAntalLFeil() === 1;
                }

                function varToFeilNaEnFeilStodPaForsteFeil() {
                    return feilHarBlittRettet && scope.feil.navaerende === 0 && totalAntalLFeil() === 1;
                }

                function erPaSisteFeil() {
                    return scope.feil.navaerende === totalAntalLFeil() - 1;
                }

                function varIkkePaSisteFeil() {
                    return scope.feil.navaerende < (totalAntalLFeil() - 1);
                }

                function sisteFeilBleRettetOgStodPaSisteFeil() {
                    return scope.feil.navaerende === totalAntalLFeil();
                }

                function sisteFeilBleRettetOgStarPaNestSisteFeilMedFlereFeilEnnTre() {
                    return scope.feil.navaerende + 2 === totalAntalLFeil() && !treFeilIgjenOgMidtersteFeilRettet();
                }

                function treFeilIgjenOgMidtersteFeilRettet() {
                    return scope.feil.navaerende === 1 && totalAntalLFeil() === 3;
                }
                function sisteFeilBleRettetOgStodPaNestSisteFeil() {
                    return scope.feil.navaerende + 1 === totalAntalLFeil();
                }

                function erPaaStartenAvFeilmeldingene() {
                    return  scope.feil.navaerende < 1 && skalDeaktivereForrigeKnapp;
                }

                function stodPaNestForsteOgForsteFeilBleRettet() {
                    return scope.feil.navaerende === 1 && feilHarBlittRettet && totalAntalLFeil() > 1;
                }

                function stodPaForsteFeilBleRettet() {
                    return scope.feil.navaerende === 0 && feilHarBlittRettet;
                }

                function stodPaNestForsteOgForsteFeilBleRettetEnFeilIgjen() {
                    return scope.feil.navaerende === 1 && feilHarBlittRettet && totalAntalLFeil() === 2;
                }

                scope.$watch(function () {
                    return antallFeil();
                }, function () {
                    if (feilBlittRettet()) {
                        feilHarBlittRettet = true;
                        if (scope.feil.navaerende === 0) {
                            skalDeaktivereForrigeKnapp = true;
                            if (totalAntalLFeil() === 2) {
                                stodPaforsteRettetEnFeilEnFeilIgjen = true;
                            }
                        } else if (erPaSisteFeil() || sisteFeilBleRettetOgStodPaSisteFeil() || sisteFeilBleRettetOgStarPaNestSisteFeilMedFlereFeilEnnTre()) {
                            scope.skalDeaktivereNesteKnapp = true;
                        }
                        if (stodPaNestForsteOgForsteFeilBleRettetEnFeilIgjen()) {
                            skalDeaktivereForrigeKnapp = true;
                            scope.skalDeaktivereNesteKnapp = false;
                            scope.feil.navaerende = 0;

                        } else if (stodPaNestForsteOgForsteFeilBleRettet()) {
                            leggTilMarkeringAvFeilmelding(-1, false);
                            skalDeaktivereForrigeKnapp = true;
                            nestForsteOgForsteRettet = true;
                        }
                    }
                    if (totalAntalLFeil() === 0) {
                        scope.feil.skalViseStickyFeilmeldinger = false;
                    }
                    scope.feil.antallFeil = antallFeil('.feil');
                });

                scope.skalVises = function () {
                    return scope.feil.antallFeil > 0 &&
                        scope.feil.skalViseStickyFeilmeldinger;
                };

                scope.skalDeaktivereForrigeKnapp = function () {
                    return erPaaStartenAvFeilmeldingene();
                };

                function leggTilMarkeringAvFeilmelding(verdi, skalScrolle) {
                    var elementerMedFeil = elem.find('.form-linje.feil, .form-linje.feilstyling');
                    var elementerMedGyldigFeil = elementerMedFeil.not('.ng-hide');
                    $(elementerMedGyldigFeil[scope.feil.navaerende]).removeClass('aktiv-feilmelding');
                    scope.feil.navaerende = scope.feil.navaerende + verdi;
                    $(elementerMedGyldigFeil[scope.feil.navaerende]).addClass('aktiv-feilmelding');

                    if (bolkMedNesteFeilErLukket(elementerMedGyldigFeil)) {
                        apneBolk(elementerMedGyldigFeil);
                    }

                    if (skalScrolle) {
                        scrollToElement($(elementerMedGyldigFeil[scope.feil.navaerende]), 300);
                    }
                    giFokus(elementerMedGyldigFeil);
                }

                function giFokus(element) {
                    $(element[scope.feil.navaerende]).find(':input').not('[type=hidden]').first().focus();
                }

                function totalAntalLFeil() {
                    return scope.feil.antallFeil;
                }

                function feilBlittRettet() {
                    return antallFeil() < scope.feil.antallFeil;
                }

                function antallFeil() {
                    var elementMedFeil = elem.find('.form-linje.feil, .form-linje.feilstyling');
                    return elementMedFeil.not('.ng-hide').length;
                }

                function bolkMedNesteFeilErLukket(bolk) {
                    return !($(bolk[scope.feil.navaerende]).closest('.accordion-group').hasClass('open'));
                }

                function apneBolk(bolk) {
                    scope.apneTab($(bolk[scope.feil.navaerende]).closest('.accordion-group').attr('id'));
                }
            }
        };
    }])
    .animation('.sticky-feilmelding', ['$timeout', function ($timeout) {
        return {
            addClass: function (element, className, done) {
                if (className === 'ng-hide') {
                    element.slideUp();
                } else {
                    done();
                }
            },
            removeClass: function (element, className, done) {
                if (className === 'ng-hide') {
                    $timeout(function () {
                        element.removeClass('ng-hide');
                        element.slideDown();
                    }, 1500);
                } else {
                    done();
                }
            }
        };
    }]);
