angular.module('nav.reellarbeidssoker',[])
    .controller('ReellarbeidssokerCtrl', ['$scope', function ($scope) {
        $('#123woot').select2();

        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        $scope.validerReellarbeidssoker = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'reell-arbeidssoker');
    }]);