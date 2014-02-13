angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.alder = parseInt(data.finnFaktum('personalia').properties.alder);
//        For testing av alder:
        //$scope.alder = 59;
        $scope.deltidannen = data.finnFaktum('reellarbeidssoker.villigdeltid.annensituasjon');
        $scope.pendleannen = data.finnFaktum('reellarbeidssoker.villigdeltid.annensituasjon');
        $scope.soknadId = data.soknad.soknadId;

		$scope.navigering = {nesteside: 'arbeidsforhold'};
		$scope.sidedata = {navn: 'reellarbeidssoker'};

		var deltidnokler = ['reellarbeidssoker.villigdeltid.reduserthelse', 'reellarbeidssoker.villigdeltid.omsorgbarnunder1aar', 'reellarbeidssoker.villigdeltid.eneansvarbarnunder5skoleaar', 'reellarbeidssoker.villigdeltid.eneansvarbarnopptil18aar', 'reellarbeidssoker.villigdeltid.omsorgansvar', 'reellarbeidssoker.villigdeltid.annensituasjon'];
		var pendlenokler = ['reellarbeidssoker.villigpendle.reduserthelse', 'reellarbeidssoker.villigpendle.omsorgbarnunder1aar', 'reellarbeidssoker.villigpendle.eneansvarbarnunder5skoleaar', 'reellarbeidssoker.villigpendle.omsorgbarnopptil10', 'reellarbeidssoker.villigpendle.eneansvarbarnopptil18aar', 'reellarbeidssoker.villigpendle.omsorgansvar', 'reellarbeidssoker.villigpendle.annensituasjon' ];

		$scope.harHuketAvCheckboksDeltid = {value: ''};
		$scope.harHuketAvCheckboksPendle = {value: ''};

		if (erCheckboxerAvhuket(deltidnokler)) {
			$scope.harHuketAvCheckboksDeltid.value = true;
		}

		if (erCheckboxerAvhuket(pendlenokler)) {
			$scope.harHuketAvCheckboksPendle.value = true;
		}

		$scope.valider = function (skalScrolle) {
			var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('reellarbeidssoker');
                $scope.settValidert('reellarbeidssoker');

            } else {
                $scope.apneTab('reellarbeidssoker');
            }
		};

		$scope.erUnder60Aar = function () {
			return $scope.alder < 60;
		};

		$scope.erOver59Aar = function () {
            return $scope.alder > 59;
		};

        $scope.harValgtAnnetUnntakDeltid = function () {
             if ($scope.deltidannen != undefined)
             {
                return $scope.deltidannen.value === 'true';
             }
            else
             {
                 return false;
             }
        };

        $scope.harValgtAnnetUnntakPendle = function () {
            if ($scope.pendleannen != undefined)
            {
                return $scope.pendleannen.value === 'true';
            }
            else
            {
                return false;
            }
        };


		$scope.endreDeltidsAarsaker = function () {
			var minstEnDeltidCheckboksAvhuket = erCheckboxerAvhuket(deltidnokler);
			if (minstEnDeltidCheckboksAvhuket) {
				$scope.harHuketAvCheckboksDeltid.value = true;
			} else {
				$scope.harHuketAvCheckboksDeltid.value = '';
			}
		};

		$scope.endrePendleAarsaker = function () {
			var minstEnPendleCheckboksAvhuket = erCheckboxerAvhuket(pendlenokler);
			if (minstEnPendleCheckboksAvhuket) {
				$scope.harHuketAvCheckboksPendle.value = true;
			} else {
				$scope.harHuketAvCheckboksPendle.value = '';
			}
		};

        $scope.trengerUtalelseFraFagpersonellDeltid = function() {
            var villigdeltidHelse = data.finnFaktum('reellarbeidssoker.villigdeltid.reduserthelse');
            var villigdeltidBarn18 = data.finnFaktum('reellarbeidssoker.villigdeltid.eneansvarbarnopptil18aar');
            var villigdeltidOmsorg = data.finnFaktum('reellarbeidssoker.villigdeltid.omsorgansvar');
            var villigdeltidAnnen = data.finnFaktum('reellarbeidssoker.villigdeltid.annensituasjon');
            
            return (villigdeltidHelse != null && villigdeltidHelse.value == "true") ||
                (villigdeltidBarn18 != null && villigdeltidBarn18.value == "true") ||
                (villigdeltidOmsorg != null && villigdeltidOmsorg.value == "true") ||
                (villigdeltidAnnen != null && villigdeltidAnnen.value == "true");
        }

        $scope.trengerUtalelseFraFagpersonellPendle = function() {
            var villigpendleHelse = data.finnFaktum('reellarbeidssoker.villigpendle.reduserthelse');
            var villigpendleBarn18 = data.finnFaktum('reellarbeidssoker.villigpendle.eneansvarbarnopptil18aar');
            var villigpendleOmsorg = data.finnFaktum('reellarbeidssoker.villigpendle.omsorgansvar');
            var villigpendleAnnen = data.finnFaktum('reellarbeidssoker.villigpendle.annensituasjon');

            return (villigpendleHelse != null && villigpendleHelse.value== "true") ||
                (villigpendleBarn18 != null && villigpendleBarn18.value == "true") ||
                (villigpendleOmsorg != null && villigpendleOmsorg.value == "true") ||
                (villigpendleAnnen != null && villigpendleAnnen.value == "true");
        }

        $scope.kanIkkeTaAlleTyperArbeid = function() {
            var villighelse = data.finnFaktum('reellarbeidssoker.villighelse');
            return villighelse != null && villighelse.value == "false";
        }

		function erCheckboxerAvhuket(checkboxNokler) {
			var minstEnAvhuket = false;
			var fakta = {};
			data.fakta.forEach(function (faktum) {
				if (checkboxNokler.indexOf(faktum.key >= 0)) {
					fakta[faktum.key] = faktum;
				}
			});

			for (var i = 0; i < checkboxNokler.length; i++) {
				var nokkel = checkboxNokler[i];
				if (fakta[nokkel] && checkTrue(fakta[nokkel].value)) {
					minstEnAvhuket = true;
				}
			}
			return minstEnAvhuket;
		}
	}]);
