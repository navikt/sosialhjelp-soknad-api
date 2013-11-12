angular.module('nav.reellarbeidssoker',[])
    .controller('ReellarbeidssokerCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};
        
        var nokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'annensituasjon'];

        $scope.validerReellarbeidssoker = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            if($scope.soknadData.fakta.villigdeltid.value == 'false') {
                form.$setValidity("reellarbeidssoker.villigdeltid.false.minstEnAvhuket.feilmelding", minstEnAvhuket);    
            } else {
                form.$setValidity("reellarbeidssoker.villigdeltid.false.minstEnAvhuket.feilmelding", true);    
            }
            

            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'reell-arbeidssoker');

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