angular.module('nav.dagpenger', [])
    .controller('DagpengerCtrl', ['$scope', '$location', '$timeout', function ($scope, $location, $timeout) {

        $scope.validerDagpenger = function(form) {
            $scope.$broadcast('VALIDER_YTELSER', form.ytelserForm);
            $scope.$broadcast('VALIDER_REELLARBEIDSSOKER', form.reellarbeidssokerForm);

            $timeout(function() {
                $scope.validateForm(form.$invalid);
                $scope.runValidation();
                if(form.$valid) {
                    $location.path("/vedlegg/" + $scope.soknadData.soknadId);
                }
            }, 100);

        }
    }])
