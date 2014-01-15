angular.module('nav.reellarbeidssoker', [])
    .controller('ReellarbeidssokerCtrl', ['$scope', 'personalia', 'data', function ($scope, personalia, data) {
        $scope.alder = personalia.alder;
//        For testing av alder:
//        $scope.alder = 59;

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

		$scope.$on('VALIDER_REELLARBEIDSSOKER', function () {
			$scope.validerReellarbeidssoker(false);
		});

		$scope.validerOgSettModusOppsummering = function (form) {
			$scope.validateForm(form.$invalid);
			$scope.validerReellarbeidssoker(true);
		};

		$scope.validerReellarbeidssoker = function (skalScrolle) {
			$scope.runValidation(skalScrolle);
		};

		$scope.erUnder60Aar = function () {
			return $scope.alder < 60;
		};

		$scope.erOver59Aar = function () {
			return $scope.alder > 59;
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
