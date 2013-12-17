angular.module('nav.barnetillegg',[])
    .controller('BarnetilleggCtrl', ['$scope', function ($scope) {
        if ($scope.soknadData.fakta.barn) {
                angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
                    value.value = angular.fromJson(value.value);
                });
            }       

        $scope.erGutt = function(barn) {
            return barn.value.kjonn == "gutt";
        }

        $scope.erJente = function(barn) {
            return barn.value.kjonn == "jente";
        }

        $scope.validerOgSettModusOppsummering = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.validerBarnetillegg(true);
        }

        $scope.validerBarnetillegg = function() {
            $scope.runValidation();
        }
    }]);