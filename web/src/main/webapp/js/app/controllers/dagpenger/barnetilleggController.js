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

        $scope.validerBarnetillegg = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", ['barnetillegg']);
    }]);