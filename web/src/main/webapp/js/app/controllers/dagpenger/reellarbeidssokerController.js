angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        var deltidnokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'annensituasjon'];
        var pendlenokler = ['pendlereduserthelse', 'pendleomsorgbarnunder1aar', 'pendleomsorgbarnopptil10', 'pendleeneansvarbarnunder5skoleaar',
            'pendleeneansvarbarnopptil18aar', 'pendleannensituasjon', 'pendleomsorgansvar' ];

        $scope.validerReellarbeidssoker = function (form) {
            if ($scope.soknadData.fakta.villigdeltid) {
                var minstEnDeltidAvhuket = $scope.erCheckboxerAvhuket(deltidnokler);
                if ($scope.soknadData.fakta.villigdeltid.value == 'false') {
                    form.$setValidity("reellarbeidssoker.villigdeltid.false.minstEnAvhuket.feilmelding", minstEnDeltidAvhuket);
                } else {
                    form.$setValidity("reellarbeidssoker.villigdeltid.false.minstEnAvhuket.feilmelding", true);
                }
            }

            if ($scope.soknadData.fakta.villigpendle) {
                var minstEnPendleAvhuket = $scope.erCheckboxerAvhuket(pendlenokler);
                if ($scope.soknadData.fakta.villigpendle.value == 'false') {
                    form.$setValidity("reellarbeidssoker.villigdpendle.false.minstEnAvhuket.feilmelding", minstEnPendleAvhuket);
                } else {
                    form.$setValidity("reellarbeidssoker.villigpendle.false.minstEnAvhuket.feilmelding", true);
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
    }]);