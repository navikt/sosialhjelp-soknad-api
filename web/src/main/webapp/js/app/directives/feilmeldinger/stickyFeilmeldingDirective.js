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
                var elem = element.next();
                var bolker = $('[data-accordion-group]');

                $scope.$on('VALIDER_DAGPENGER', function (scope, form) {
                    $scope.feil.antallFeil = elem.find('.form-linje.feil').length;
                    $scope.feil.antallFeilStyling =  elem.find('.form-linje.feilstyling').length;

                    var idBolkerMedFeil = []
                    var idBolkerUtenFeil = []

                    var bolkerMedFeil = bolker.has('.form-linje.feil, .form-linje.feilstyling');
                    var bolkerUtenFeil = bolker.not(bolkerMedFeil);

                    bolkerMedFeil.each(function() {
                        idBolkerMedFeil.push(this.id);
                    });

                    bolkerUtenFeil.each(function() {
                        idBolkerUtenFeil.push(this.id);
                    });

                    $scope.$broadcast('OPEN_TAB', idBolkerMedFeil);
                    $scope.$broadcast('CLOSE_TAB', idBolkerUtenFeil);


                });

                $scope.$watch(function () {
                    return elem.find('.form-linje.feil').length;
                }, function () {
                    $scope.feil.antallFeil =  elem.find('.form-linje.feil').length;
                });

                $scope.$watch(function () {
                   return elem.find('.form-linje.feilstyling').length;
                }, function () {
                    $scope.feil.antallFeilStyling =  elem.find('.form-linje.feilstyling').length;
                });

                $scope.skalVises = function() {
                    return $scope.feil.antallFeil + $scope.feil.antallFeilStyling > 0;
                }
            }


        }
    }]);