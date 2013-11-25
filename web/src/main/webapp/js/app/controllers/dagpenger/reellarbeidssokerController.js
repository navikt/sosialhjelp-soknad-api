angular.module('nav.reellarbeidssoker',[])
    .controller('ReellarbeidssokerCtrl', ['$scope','data', function ($scope, data) {
        $scope.alder = data.alder.alder;
        //For testing av alder:
        //$scope.alder =59;

        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};
        
        var deltidnokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'omsorgansvar', 'annensituasjon'];
        var pendlenokler = ['pendlereduserthelse', 'pendleomsorgbarnunder1aar', 'pendleomsorgbarnopptil10', 'pendleeneansvarbarnunder5skoleaar', 
                            'pendleeneansvarbarnopptil18aar', 'pendleannensituasjon', 'pendleomsorgansvar' ];

        $scope.validerReellarbeidssoker = function(form) {
            if($scope.soknadData.fakta.villigdeltid && $scope.erUnder60Aar()) {
                var minstEnDeltidAvhuket = $scope.erCheckboxerAvhuket(deltidnokler);
                if($scope.soknadData.fakta.villigdeltid.value == 'false') {
                    form.$error['reellarbeidssoker'][0].$invalid = !minstEnDeltidAvhuket;
                    form.$error['reellarbeidssoker'][0].$valid = minstEnDeltidAvhuket;
                } else {
                    form.$error['reellarbeidssoker'][0].$valid = true;
                    form.$error['reellarbeidssoker'][0].$invalid = false;
                }
            }

            if($scope.soknadData.fakta.villigpendle && $scope.erUnder60Aar()) {
                var minstEnPendleAvhuket = $scope.erCheckboxerAvhuket(pendlenokler);
                if($scope.soknadData.fakta.villigpendle.value == 'false') {
                    form.$error['reellarbeidssoker'][1].$invalid = !minstEnPendleAvhuket;
                    form.$error['reellarbeidssoker'][1].$valid = minstEnPendleAvhuket;
                } else {
                    form.$error['reellarbeidssoker'][1].$invalid = false;
                    form.$error['reellarbeidssoker'][1].$valid = true;
                }
            }
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        $scope.$emit("OPEN_TAB", 'reell-arbeidssoker');

        $scope.erCheckboxerAvhuket = function (checkboxNokler) {
            var minstEnAvhuket = false;
            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            return minstEnAvhuket;
        }

        $scope.erUnder60Aar = function() {
            return $scope.alder < 60;
        }

        $scope.erOver59Aar = function() {
            return $scope.alder > 59;
        }
    }]);