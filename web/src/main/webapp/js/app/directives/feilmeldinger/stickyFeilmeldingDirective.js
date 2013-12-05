angular.module('nav.stickyFeilmelding', [])

    .directive('stickyFeilmelding', [function () {
        return {
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/stickyFeilmeldingTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function ($scope, element, attrs, ctrl) {
                $scope.feil = [];
                $scope.feil.antallFeil = 0;
                $scope.feil.antallFeilStyling = 0;
                $scope.feil.navaerende = -1;

                var elem = element.next();
                var bolker = $('[data-accordion-group]');

                $scope.$on('VALIDER_DAGPENGER', function (scope, form) {
                    $scope.feil.antallFeil = elem.find('.form-linje.feil').length;
                    $scope.feil.antallFeilStyling = elem.find('.form-linje.feilstyling').length;

                    var idBolkerMedFeil = []
                    var idBolkerUtenFeil = []

                    var bolkerMedFeil = bolker.has('.form-linje.feil, .form-linje.feilstyling');
                    var bolkerUtenFeil = bolker.not(bolkerMedFeil);

                    bolkerMedFeil.each(function () {
                        idBolkerMedFeil.push(this.id);
                    });

                    bolkerUtenFeil.each(function () {
                        idBolkerUtenFeil.push(this.id);
                    });

                    $scope.$broadcast('OPEN_TAB', idBolkerMedFeil);
                    $scope.$broadcast('CLOSE_TAB', idBolkerUtenFeil);
                });

                $scope.forrige = function () {
                    if ($scope.feil.navaerende > 0) {
                        $scope.feil.navaerende = $scope.feil.navaerende - 1;
                        scrollToElement($(bolker.find('.form-linje.feil, .form-linje.feilstyling')[$scope.feil.navaerende]));
                    }
                }

                $scope.neste = function () {
                    if($scope.feil.navaerende < (totalAntalLFeil() -1)) {
                        $scope.feil.navaerende = $scope.feil.navaerende + 1;
                        scrollToElement($(bolker.find('.form-linje.feil, .form-linje.feilstyling')[$scope.feil.navaerende]));
                    } else if ($scope.feil.navaerende < totalAntalLFeil()) {
                        scrollToElement($(bolker.find('.form-linje.feil, .form-linje.feilstyling')[$scope.feil.navaerende]));
                    } else if ($scope.feil.navaerende === totalAntalLFeil()) {
                        $scope.feil.navaerende = $scope.feil.navaerende - 1;
                        scrollToElement($(bolker.find('.form-linje.feil, .form-linje.feilstyling')[$scope.feil.navaerende]));
                    }
                }

                $scope.$watch(function () {
                    return elem.find('.form-linje.feil').length;
                }, function () {
                    if(elem.find('.form-linje.feil').length < $scope.feil.antallFeil) {
                        $scope.feil.navaerende = $scope.feil.navaerende - 1;
                    }
                    $scope.feil.antallFeil = elem.find('.form-linje.feil').length;
                });

                $scope.$watch(function () {
                    return elem.find('.form-linje.feilstyling').length;
                }, function () {
                    if(elem.find('.form-linje.feilstyling').length < $scope.feil.antallFeilStyling) {
                        $scope.feil.navaerende = $scope.feil.navaerende - 1;
                    }
                    $scope.feil.antallFeilStyling = elem.find('.form-linje.feilstyling').length;
                });

                $scope.skalVises = function () {
                    return totalAntalLFeil() > 0;
                }

                function totalAntalLFeil () {
                    return $scope.feil.antallFeil + $scope.feil.antallFeilStyling;
                }
            }


        }
    }]);