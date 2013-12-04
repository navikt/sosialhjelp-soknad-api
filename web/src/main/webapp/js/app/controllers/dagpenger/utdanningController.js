angular.module('nav.utdanning',[])
    .controller('UtdanningCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'ytelser'};
        $scope.sidedata = {navn: 'utdanning'};

        $scope.$on('VALIDER_UTDANNING', function (scope, form) {
            $scope.validerUtdanning(form, false);
        });

        $scope.validerUtdanning = function(form, skalScrolle) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation(skalScrolle);

        }

        $scope.hvisIkkeUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'ikkeUtdanning';
            }
            return false;
        }

        $scope.hvisAvsluttetUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'avsluttetUtdanning';
            }
            return false;
        }

        $scope.hvisUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'underUtdanning';
            }
            return false;
        }
    }]);