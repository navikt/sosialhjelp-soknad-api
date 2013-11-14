angular.module('nav.utdanning',[])
    .controller('UtdanningCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'ytelser'};
        $scope.sidedata = {navn: 'utdanning'};

    var nokler = ['underUtdanningKveld', 'underUtdanningKveldPaabegynt', 'underUtdanningFulltid', 'underUtdanningKortvarig', 'underUtdanningNorsk', 'ingenYtelse' ];

    $scope.validerUtdanning = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            form.$setValidity("utdanning.minstEnAvhuket.feilmelding", minstEnAvhuket);
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
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

    $scope.erCheckboxerAvhuket = function(checkboxNokler) {
        var minstEnAvhuket = false;
        for(var i= 0; i < checkboxNokler.length; i++) {
            var nokkel = checkboxNokler[i];
            if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                minstEnAvhuket = true;
            }
        }
        return minstEnAvhuket;
    }
    }]);