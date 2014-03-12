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

                scope.lukk = function () {
                    scope.feil.skalViseStickyFeilmeldinger = false;
                };

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

                    if (totalAntallFeil() === 1) {
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
                    if (!(feilHarBlittRettet && scope.feil.navaerende === totalAntallFeil())) {
                        settNavaerendeFeil();
                        handterNeste();
                    } else if (feilHarBlittRettet && scope.feil.navaerende === 1 && totalAntallFeil() === 1) {
                        leggTilMarkeringAvFeilmelding(-1, true);
                    }
                };

                function settNavaerendeFeil() {
                    if (feilHarBlittRettet && scope.feil.navaerende > 0 && varIkkePaSisteFeil()) {
                        scope.feil.navaerende = scope.feil.navaerende - 1;
                    }
                }

                function handterNeste() {
                    if (varToFeilMenEnFeilErRettet()) {
                        var idx = stodPaForsteFeil() ? 0 : 1;
                        leggTilMarkeringAvFeilmelding(idx, true);
                        scope.skalDeaktivereNesteKnapp = true;
                        feilHarBlittRettet = false;
                    } else if (sisteFeilBleRettetOgStodPaNestSisteFeil()) {
                        scope.skalDeaktivereNesteKnapp = true;
                        scope.feil.navaerende = scope.feil.navaerende + 1;
                        feilHarBlittRettet = true;

                    } else if (nestForsteOgForsteRettet) {
                        scope.feil.navaerende = 0;
                        leggTilMarkeringAvFeilmelding(1, true);
                        nestForsteOgForsteRettet = false;
                        feilHarBlittRettet = false;

                    } else if (stodPaForsteFeilBleRettet()) {
                        leggTilMarkeringAvFeilmelding(0, true);
                        feilHarBlittRettet = false;

                    } else if (varIkkePaSisteFeil()) {
                        leggTilMarkeringAvFeilmelding(1, true);
                        feilHarBlittRettet = false;

                        if (erPaSisteFeil()) {
                            scope.skalDeaktivereNesteKnapp = true;
                        }
                    } else if (erPaSisteFeil()) {
                        leggTilMarkeringAvFeilmelding(0, false);
                        scope.skalDeaktivereNesteKnapp = true;
                        feilHarBlittRettet = false;

                    } else if (sisteFeilBleRettetOgStodPaSisteFeil()) {
                        leggTilMarkeringAvFeilmelding(-1, true);
                        scope.skalDeaktivereNesteKnapp = true;
                        feilHarBlittRettet = false;

                    }
                }

                function sisteFeilAkkuratRettetOgFlereFeilIgjen() {
                    return scope.feil.navaerende === totalAntallFeil() && feilHarBlittRettet;
                }

                function enFeilIgjen() {
                    return scope.feil.navaerende === 0 && totalAntallFeil() === 1;
                }

                function varToFeilMenEnFeilErRettet() {
                    return feilHarBlittRettet && totalAntallFeil() === 1;
                }

                function stodPaForsteFeil() {
                    return scope.feil.navaerende === 0;
                }

                function erPaSisteFeil() {
                    return scope.feil.navaerende === totalAntallFeil() - 1;
                }

                function varIkkePaSisteFeil() {
                    return scope.feil.navaerende < (totalAntallFeil() - 1);
                }

                function sisteFeilBleRettetOgStodPaSisteFeil() {
                    return scope.feil.navaerende === totalAntallFeil();
                }

                function sisteFeilBleRettetOgStarPaNestSisteFeilMedFlereFeilEnnTre() {
                    return scope.feil.navaerende + 2 === totalAntallFeil() && !treFeilIgjenOgMidtersteFeilRettet();
                }

                function treFeilIgjenOgMidtersteFeilRettet() {
                    return scope.feil.navaerende === 1 && totalAntallFeil() === 3;
                }
                function sisteFeilBleRettetOgStodPaNestSisteFeil() {
                    return feilHarBlittRettet && scope.feil.navaerende + 1 === totalAntallFeil();
                }

                function erPaaStartenAvFeilmeldingene() {
                    return  scope.feil.navaerende < 1 && skalDeaktivereForrigeKnapp;
                }

                function stodPaNestForsteOgForsteFeilBleRettet() {
                    return scope.feil.navaerende === 1 && feilHarBlittRettet && totalAntallFeil() > 1;
                }

                function stodPaForsteFeilBleRettet() {
                    return scope.feil.navaerende === 0 && feilHarBlittRettet;
                }

                function stodPaNestForsteOgForsteFeilBleRettetEnFeilIgjen() {
                    return scope.feil.navaerende === 1 && feilHarBlittRettet && totalAntallFeil() === 2;
                }

                scope.$watch(function () {
                    return antallFeil();
                }, function () {
                    if (feilBlittRettet()) {
                        feilHarBlittRettet = true;
                        if (scope.feil.navaerende === 0) {
                            skalDeaktivereForrigeKnapp = true;
                            if (totalAntallFeil() === 2) {
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
                    if (totalAntallFeil() === 0) {
                        scope.feil.skalViseStickyFeilmeldinger = false;
                    }
                    scope.feil.antallFeil = antallFeil();
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

                function totalAntallFeil() {
                    return scope.feil.antallFeil;
                }

                function feilBlittRettet() {
                    return antallFeil() < scope.feil.antallFeil;
                }
                function antallFeil() {
                    elem = element.next();
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
