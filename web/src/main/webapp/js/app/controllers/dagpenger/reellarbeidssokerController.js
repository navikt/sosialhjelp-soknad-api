angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.alder = data.alder.alder;
//        For testing av alder:
//        $scope.alder = 59;

        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        var deltidnokler = ['reduserthelse', 'omsorgbarnunder1aar', 'eneansvarbarnunder5skoleaar', 'eneansvarbarnopptil18aar', 'omsorgansvar', 'annensituasjon'];
        var pendlenokler = ['pendlereduserthelse', 'pendleomsorgbarnunder1aar', 'pendleomsorgbarnopptil10', 'pendleeneansvarbarnunder5skoleaar',
            'pendleeneansvarbarnopptil18aar', 'pendleannensituasjon', 'pendleomsorgansvar' ];

        $scope.harHuketAvCheckboksDeltid = {value : ''};
        $scope.harHuketAvCheckboksPendle = {value : ''};

        if (erCheckboxerAvhuket(deltidnokler)){
            $scope.harHuketAvCheckboksDeltid.value = true;
        }

        if (erCheckboxerAvhuket(pendlenokler)) {
            $scope.harHuketAvCheckboksPendle.value = true;
        }

        $scope.$on('VALIDER_REELLARBEIDSSOKER', function () {
            $scope.validerReellarbeidssoker(false);
        });

        $scope.validerOgSettModusOppsummering = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.validerReellarbeidssoker(true);
        }

        $scope.validerReellarbeidssoker = function (skalScrolle) {
            $scope.runValidation(skalScrolle);
        };

        $scope.erUnder60Aar = function () {
            return $scope.alder < 60;
        }

        $scope.erOver59Aar = function () {
            return $scope.alder > 59;
        }

        $scope.endreDeltidsAarsaker = function (form) {
            var minstEnDeltidCheckboksAvhuket = erCheckboxerAvhuket(deltidnokler);
            if(minstEnDeltidCheckboksAvhuket) {
                $scope.harHuketAvCheckboksDeltid.value = true;
            } else {
                $scope.harHuketAvCheckboksDeltid.value = '';
            }
        }

        $scope.endrePendleAarsaker = function (form) {
            var minstEnPendleCheckboksAvhuket = erCheckboxerAvhuket(pendlenokler);
            if(minstEnPendleCheckboksAvhuket) {
                $scope.harHuketAvCheckboksPendle.value = true;
            } else {
                $scope.harHuketAvCheckboksPendle.value = '';
            }
        }

        function erCheckboxerAvhuket(checkboxNokler) {
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