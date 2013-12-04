angular.module('nav.dagpenger', [])
    .controller('DagpengerCtrl', ['$scope', '$location', '$timeout', function ($scope, $location, $timeout) {

        $scope.validerDagpenger = function(form) {
            $scope.$broadcast('VALIDER_YTELSER', form.ytelserForm);
            $scope.$broadcast('VALIDER_UTDANNING', form.utdanningForm);
            $scope.$broadcast('VALIDER_ARBEIDSFORHOLD', form.arbeidsforholdForm);
            $scope.$broadcast('VALIDER_EGENNAERING', form.egennaeringForm);
            $scope.$broadcast('VALIDER_VERNEPLIKT', form.vernepliktigForm);
//            $scope.$broadcast('VALIDER_FRIVILLIG', form.frivilligForm);
//            $scope.$broadcast('VALIDER_PERSONALIA', form.personaliaForm);
            $scope.$broadcast('VALIDER_REELLARBEIDSSOKER', form.reellarbeidssokerForm);
            $scope.$broadcast('VALIDER_DAGPENGER', form);

            $timeout(function() {
                $scope.validateForm(form.$invalid);
                if(form.$valid) {
                    $location.path("/vedlegg/" + $scope.soknadData.soknadId);
                } else {
                    scrollToElement($('.accordion-group.open').first());
                }
            }, 500);
        }
    }])
