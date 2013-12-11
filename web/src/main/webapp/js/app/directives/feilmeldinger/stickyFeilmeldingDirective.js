angular.module('nav.stickyFeilmelding', [])

    .directive('stickyFeilmelding', ['$timeout', function ($timeout) {
        return {
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function (scope, element, attrs, ctrl) {
                scope.feil = [];
                scope.feil.antallFeilMedKlasseFeil = 0;
                scope.feil.antallFeilMedKlasseFeilstyling = 0;
                scope.feil.skalViseStickyFeilmeldinger = false;
                scope.feil.navaerende = -1;

                var elem = element.next();
                var bolker = $('[data-accordion-group]');
                var skalDeaktivereForrigeKnapp = true;
                var skalDeaktivereNesteKnapp = false;
                var feilHarBlittRettet = false;

                scope.$on('VALIDER_DAGPENGER', function (eventscope, form) {
                    elem = element.next();
                    bolker = $('[data-accordion-group]');

                    scope.feil.skalViseStickyFeilmeldinger = true;
                    scope.feil.antallFeilMedKlasseFeil = elem.find('.form-linje.feil').length;
                    scope.feil.antallFeilMedKlasseFeilstyling = elem.find('.form-linje.feilstyling').length;

                    var idBolkerMedFeil = [];
                    var idAlleBolker = [];

                    var bolkerMedFeil = bolker.has('.form-linje.feil, .form-linje.feilstyling');

                    bolkerMedFeil.each(function () {
                        idBolkerMedFeil.push(this.id);
                    });

                    bolker.each(function () {
                        idAlleBolker.push(this.id);
                    });

                    scope.$broadcast('CLOSE_TAB', idAlleBolker);
                    $timeout(function() {
                        scope.$broadcast('OPEN_TAB', idBolkerMedFeil, 800);
                    }, 800);
                });

                scope.forrige = function () {
                    if (!(feilHarBlittRettet && scope.feil.navaerende < 1)) {
                        if (scope.feil.navaerende > 0) {
                            leggTilMarkeringAvFeilmelding(-1)
                            feilHarBlittRettet = false;
                        } else if (scope.feil.navaerende === 0) {
                            skalDeaktivereForrigeKnapp = true;
                            feilHarBlittRettet = false;
                            leggTilMarkeringAvFeilmelding(0)
                        }
                    }
                    skalDeaktivereNesteKnapp = false;
                }

                scope.neste = function () {
                    if (!(feilHarBlittRettet && scope.feil.navaerende == totalAntalLFeil())) {
                        if (feilHarBlittRettet && scope.feil.navaerende > -1) {
                            scope.feil.navaerende = scope.feil.navaerende - 1;
                        }

                        if (scope.feil.navaerende < (totalAntalLFeil() - 1)) {
                            leggTilMarkeringAvFeilmelding(1);
                        } else if (scope.feil.navaerende < totalAntalLFeil()) {
                            leggTilMarkeringAvFeilmelding(0)
                        } else if (scope.feil.navaerende === totalAntalLFeil()) {
                            leggTilMarkeringAvFeilmelding(-1)
                        }
                        feilHarBlittRettet = false;
                    }
                }

                scope.$watch(function () {
                    return antallFeilMedKlasse('.feil');
                }, function () {
                    if (feilMedKlasseFeilHarBlittRettet()) {
                        feilHarBlittRettet = true;
                        if (scope.feil.navaerende == 0) {
                            skalDeaktivereForrigeKnapp = true;
                        } else if(scope.feil.navaerende == totalAntalLFeil() -1) {
                            skalDeaktivereNesteKnapp = true
                        }
                    }
                    if (totalAntalLFeil() == 0) {
                        scope.feil.skalViseStickyFeilmeldinger = false;
                    }
                    scope.feil.antallFeilMedKlasseFeil = antallFeilMedKlasse('.feil');
                });

                scope.$watch(function () {
                    return antallFeilMedKlasse('.feilstyling');
                }, function () {
                    if (feilMedKlasseFeilstylingHarBlittRettet()) {
                        feilHarBlittRettet = true;
                        if (scope.feil.navaerende == 0) {
                            skalDeaktivereForrigeKnapp = true;
                        } else if(scope.feil.navaerende == totalAntalLFeil() -1) {
                            skalDeaktivereNesteKnapp = true
                        }
                    }
                    if (totalAntalLFeil() == 0) {
                        scope.feil.skalViseStickyFeilmeldinger = false;
                    }
                    scope.feil.antallFeilMedKlasseFeilstyling = antallFeilMedKlasse('.feilstyling');
                });

                scope.skalVises = function () {
                    return scope.feil.antallFeilMedKlasseFeil + scope.feil.antallFeilMedKlasseFeilstyling > 0 && scope.feil.skalViseStickyFeilmeldinger;
                }

                scope.skalDeaktivereNesteKnapp = function () {
                    return harKommetTilEndenAvFeilmeldinger();
                }

                scope.skalDeaktivereForrigeKnapp = function () {
                    return erPaaStartenAvFeilmeldingene();
                }

                function leggTilMarkeringAvFeilmelding(verdi) {
                    bolker = $('[data-accordion-group]');
                    var bolk = bolker.find('.form-linje.feil, .form-linje.feilstyling');

                    $(bolk[scope.feil.navaerende]).removeClass('aktiv-feilmelding');
                    scope.feil.navaerende = scope.feil.navaerende + verdi;
                    $(bolk[scope.feil.navaerende]).addClass('aktiv-feilmelding');

                    if (bolkMedNesteFeilErLukket(bolk)) {
                        apneBolk(bolk)
                    }

                    scrollToElement($(bolk[scope.feil.navaerende]), 300);
                    giFokus(bolk);
                }

                function giFokus(element) {
                    $(element[scope.feil.navaerende]).find(':input').focus();
                }

                function totalAntalLFeil() {
                    return scope.feil.antallFeilMedKlasseFeil + scope.feil.antallFeilMedKlasseFeilstyling;
                }

                function feilMedKlasseFeilHarBlittRettet() {
                    return elem.find('.form-linje.feil').length < scope.feil.antallFeilMedKlasseFeil;
                }

                function feilMedKlasseFeilstylingHarBlittRettet() {
                    return elem.find('.form-linje.feilstyling').length < scope.feil.antallFeilMedKlasseFeilstyling;
                }

                function antallFeilMedKlasse(klasse) {
                    return elem.find('.form-linje' + klasse).length;
                }

                function harKommetTilEndenAvFeilmeldinger() {
                    return scope.feil.navaerende === totalAntalLFeil() - 1 || skalDeaktivereNesteKnapp;
                }

                function erPaaStartenAvFeilmeldingene() {
                    return  scope.feil.navaerende < 1 && skalDeaktivereForrigeKnapp;
                }

                function bolkMedNesteFeilErLukket (bolk) {
                    return !($(bolk[scope.feil.navaerende]).closest('.accordion-group').hasClass('open'));
                }

                function apneBolk(bolk) {
                    scope.$broadcast('OPEN_TAB', $(bolk[scope.feil.navaerende]).closest('.accordion-group').attr('id'));
                }
            }
        }
    }])
    .animation('.sticky-feilmelding', ['$timeout', function ($timeout) {
        return {
            addClass: function (element, className, done) {
                if (className == 'ng-hide') {
                    element.slideUp();
                } else {
                    done();
                }
            },
            removeClass: function (element, className, done) {
                if (className == 'ng-hide') {
                    $timeout(function () {
                        element.removeClass('ng-hide');
                        element.slideDown();
                    }, 1500);
                } else {
                    done();
                }
            }
        }
    }]);