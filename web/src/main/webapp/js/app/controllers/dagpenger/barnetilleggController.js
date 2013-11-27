angular.module('nav.barnetillegg',[])
    .controller('BarnetilleggCtrl', ['$scope', function ($scope) {
        $scope.barn = {
            sammensattnavn: "Jens August Aker Hansen",
            fnr: ***REMOVED***,
            alder: 11,
            barnetillegg: false
        }

        $scope.validerBarnetillegg = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'barnetillegg');
    }]);