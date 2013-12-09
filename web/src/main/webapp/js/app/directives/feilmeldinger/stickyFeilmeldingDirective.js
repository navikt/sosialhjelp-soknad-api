angular.module('nav.stickyFeilmelding', [])

    .directive('stickyFeilmelding',  ['$timeout', function ($timeout) {
        return {
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function (scope, element, attrs, ctrl) {
                scope.feil = [];
                scope.feil.antallFeil = 0;
                scope.feil.antallFeilStyling = 0;
                scope.test = [];
                scope.feil.navaerende = -1;

                var elem = element.next();
                var bolker = $('[data-accordion-group]');

                scope.$on('VALIDER_DAGPENGER', function (eventscope, form) {
                    elem = element.next();
                    bolker = $('[data-accordion-group]');
                    for(var i = 0; i <  elem.find('.form-linje.feil, .form-linje.feilstyling').length; i++) {
                        scope.test.push({id: i+1, element : angular.element(elem.find('.form-linje.feil, .form-linje.feilstyling')[i])})
                    }
                    scope.feil.antallFeil = elem.find('.form-linje.feil').length;
                    scope.feil.antallFeilStyling = elem.find('.form-linje.feilstyling').length;

                    var idBolkerMedFeil = []
                    var idBolkerUtenFeil = []
                    var idAlleBolker = []

                    var bolkerMedFeil = bolker.has('.form-linje.feil, .form-linje.feilstyling');
                    var bolkerUtenFeil = bolker.not(bolkerMedFeil);

                    bolkerMedFeil.each(function () {
                        idBolkerMedFeil.push(this.id);
                    });

                    bolkerUtenFeil.each(function () {
                        idBolkerUtenFeil.push(this.id);
                    });

                    bolker.each(function () {
                        idAlleBolker.push(this.id);
                    });

                    scope.$broadcast('OPEN_TAB', idBolkerMedFeil, 800);
//                    $scope.$broadcast('CLOSE_TAB', idBolkerUtenFeil, 0);
                    scope.$broadcast('CLOSE_TAB', idAlleBolker, 0);

                });

                scope.forrige = function () {
                    var bolk = bolker.find('.form-linje.feil, .form-linje.feilstyling');
                    if (scope.feil.navaerende > 0) {
                        leggTilMarkeringAvFeilmelding( -1)
                    } else if (scope.feil.navaerende === 0 ) {
                        leggTilMarkeringAvFeilmelding( 0)
                    }
                }

                scope.neste = function () {

                    if(scope.feil.navaerende < (totalAntalLFeil() -1)) {
                        leggTilMarkeringAvFeilmelding( 1);
                    } else if (scope.feil.navaerende < totalAntalLFeil()) {
                        leggTilMarkeringAvFeilmelding( 0)
                    } else if (scope.feil.navaerende === totalAntalLFeil()) {
                        leggTilMarkeringAvFeilmelding(-1)

                    }
                }

                scope.$watch(function () {
                    return elem.find('.form-linje.feil').length;
                }, function () {
                    if(elem.find('.form-linje.feil').length < scope.feil.antallFeil) {
                        scope.feil.navaerende = scope.feil.navaerende - 1;
                    }
                    scope.feil.antallFeil = elem.find('.form-linje.feil').length;
                });

                scope.$watch(function () {
                    return elem.find('.form-linje.feilstyling').length;
                }, function () {
                    if(elem.find('.form-linje.feilstyling').length < scope.feil.antallFeilStyling) {
                        scope.feil.navaerende = scope.feil.navaerende - 1;
                    }
                    scope.feil.antallFeilStyling = elem.find('.form-linje.feilstyling').length;
                });

                scope.skalVises = function () {
                    return scope.feil.antallFeil + scope.feil.antallFeilStyling > 0;
                }

                function leggTilMarkeringAvFeilmelding(verdi) {
                    bolker = $('[data-accordion-group]');
                    var bolk = bolker.find('.form-linje.feil, .form-linje.feilstyling');
                    $(bolk[scope.feil.navaerende]).removeClass('aktiv-feilmelding');
                    scope.feil.navaerende = scope.feil.navaerende + verdi;
                    $(bolk[scope.feil.navaerende]).addClass('aktiv-feilmelding');
                    scrollToElement($(bolk[scope.feil.navaerende]), 300);
                    giFokus(bolk);
                }

                function giFokus(element) {
                    $(element[scope.feil.navaerende]).find(':input').focus();
                }
                function totalAntalLFeil () {
                    return scope.feil.antallFeil + scope.feil.antallFeilStyling;
                }

                scope.skalDeaktivereNesteKnapp = function () {
                    return scope.feil.navaerende === totalAntalLFeil()-1;
                }

                scope.skalDeaktivereForrigeKnapp = function () {
                    return scope.feil.navaerende < 1;
                }
            }
        }
    }])
    .animation('.sticky-feilmelding', ['$timeout', function($timeout) {
        return {
            addClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    element.slideUp();
                } else {
                    done();
                }
            },
            removeClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    $timeout(function() {
                        element.removeClass('ng-hide');
                        element.slideDown();
                    }, 1500);
                } else {
                    done();
                }
            }
        }
    }]);