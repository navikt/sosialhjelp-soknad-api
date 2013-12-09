angular.module('nav.barnetillegg',[])
    .controller('BarnetilleggCtrl', ['$scope', function ($scope) {
        if ($scope.soknadData.fakta.barn) {
                $scope.barn = [];
                angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
                    value.value = angular.fromJson(value.value);
                });
            }

        /*if ($scope.soknadData.fakta.barnetillegg) {
            $scope.barnetillegg = {};
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
                $scope.barnetillegg[angular.fromJson(value).fnr] = angular.fromJson(value).valgt;
            });
        } else {
            $scope.soknadData.fakta.barnetillegg = {};
             $scope.barnetillegg = {};
        }
        */
        $scope.lagreBarnetilegg = function(barn, index , event) {
            var result = {};
            result["fnr"] = barn.fnr;
            result["valgt"] = event.target.checked;

            leggTilFaktumIdIResultDersomDenFinnes(barn, result);
            $scope.$emit("LAGRE_BARNETILLEGG", {key: 'barnetillegg', value: angular.toJson(result)});
        }

        function leggTilFaktumIdIResultDersomDenFinnes(barn, result) {
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
                if(angular.fromJson(value).fnr == barn.fnr) {
                    result["faktumId"] = angular.fromJson(value).faktumId;
                }
            });
        }

        $scope.erGutt = function(barn) {
            return barn.value.kjonn == "gutt";
        }

        $scope.erJente = function(barn) {
            return barn.value.kjonn == "jente";
        }

        $scope.validerBarnetillegg = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'barnetillegg');
    }]);