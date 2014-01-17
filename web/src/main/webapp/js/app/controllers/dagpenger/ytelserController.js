angular.module('nav.ytelser', [])
	.controller('YtelserCtrl', ['$scope', 'data', function ($scope, data) {

		$scope.land = data.land;
		$scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};
		$scope.ytelserNAV = {skalViseFeilmeldingForIngenNavYtelser: false};

		var nokler = ['stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'etterlonn', 'garantilott', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];
		var undernokler = ['sykepenger', 'aap', 'uforetrygd', 'svangerskapspenger', 'foreldrepenger', 'ventelonn', 'ingennavytelser' ];

		$scope.harHuketAvCheckboksYtelse = {value: ''};
		$scope.harHuketAvCheckboksNavYtelse = {value: ''};

		if (erCheckboxerAvhuket(nokler)) {
			$scope.harHuketAvCheckboksYtelse.value = true;
			$scope.harHuketAvCheckboksNavYtelse.value = true;
		}

		$scope.$on('VALIDER_YTELSER', function () {
			$scope.validerYtelser(false);
		});

		$scope.validerOgSettModusOppsummering = function (form) {
			$scope.validateForm(form.$invalid);
			$scope.validerYtelser(true);
		};

		$scope.hvisAvtaleInngaatt = function () {
			var faktum = data.finnFaktum('ikkeavtale');
			if (faktum !== undefined && faktum.value !== undefined) {
				return faktum.value === 'false';
			}
			return false;
		};

		$scope.hvisHarDagpengerEOS = function () {
			var faktum = data.finnFaktum('dagpengerEOS');
			if (faktum !== undefined && faktum.value !== undefined) {
				return faktum.value === 'true';
			}
			return false;
		};

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
		$scope.validerYtelser = function (skalScrolle) {
			$scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
			$scope.ytelser.skalViseFeilmeldingForIngenNavYtelser = false;
			$scope.ytelser.skalViseFeilmeldingForAvtale = false;
			$scope.runValidation(skalScrolle);
		};

//      kjøres hver gang det skjer en endring på checkboksene
		$scope.endreYtelse = function (form) {
			// Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
			var ytelserNokler = nokler.slice(0, nokler.length - 1);
			var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

			if (harIkkeValgtYtelse) {
				$scope.harHuketAvCheckboksYtelse.value = '';
			} else {
				$scope.harHuketAvCheckboksYtelse.value = true;
			}

			var ingenYtelse = data.finnFaktum('ingenYtelse');
			if (sjekkOmGittEgenskapTilObjektErTrue(ingenYtelse)) {
				ingenYtelse.value = 'false';
				ingenYtelse.$save();
			}
		};

		//      kjøres hver gang det skjer en endring på 'ingenYtelse'-checkboksen
		$scope.endreIngenYtelse = function () {
			var faktum = data.finnFaktum('ingenYtelse');
			// Sjekker om en ytelse er huket av, fjerner isåfall alle som er huket av (inkluderer IKKE siste checkboksen)
			var ytelserNokler = nokler.slice(0, nokler.length - 1);
			fjernAvhuking(ytelserNokler);
			var erCheckboksForIngenYtelseHuketAv = faktum.value === 'true';

			if (erCheckboksForIngenYtelseHuketAv) {
				$scope.harHuketAvCheckboksYtelse.value = 'true';
			} else {
				$scope.harHuketAvCheckboksYtelse.value = '';
			}
			faktum.$save();
		};

		//      kjøres hver gang det skjer en endring på checkboksene for Nav Ytelser
		$scope.endreNavYtelse = function (form) {
			// Sjekker om en navytelse er huket av (inkluderer IKKE siste checkboksen)
			var ytelserNokler = undernokler.slice(0, nokler.length - 1);
			var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);
			if (harIkkeValgtYtelse) {
				$scope.harHuketAvCheckboksNavYtelse.value = '';
			} else {
				$scope.harHuketAvCheckboksNavYtelse.value = true;
			}
			var faktum = data.finnFaktum('ingennavytelser');
			if (sjekkOmGittEgenskapTilObjektErTrue(faktum)) {
				faktum.value = false;
				faktum.$save();
			}
		};

		//      kjøres hver gang det skjer en endring på 'ingenNAVYtelse'-checkboksen
		$scope.endreIngenNavYtelse = function (form) {
			var faktum = data.finnFaktum('ingennavytelser');

			// Sjekker om en ytelse er huket av, fjerner isåfall alle som er huket av (inkluderer IKKE siste checkboksen)
			var ytelserNokler = undernokler.slice(0, undernokler.length - 1);
			fjernAvhuking(ytelserNokler);
			var erCheckboksForIngenNavYtelseHuketAv = faktum.value === 'true';

			if (erCheckboksForIngenNavYtelseHuketAv) {
				$scope.harHuketAvCheckboksNavYtelse.value = 'true';
			} else {
				$scope.harHuketAvCheckboksNavYtelse.value = '';
			}
			faktum.$save();
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

		function fjernAvhuking(checkboxNokler) {
			var fakta = {};

			data.fakta.forEach(function (faktum) {
				if (checkboxNokler.indexOf(faktum.key >= 0)) {
					fakta[faktum.key] = faktum;
				}
			});

			for (var i = 0; i < checkboxNokler.length; i++) {
				var nokkel = checkboxNokler[i];
				if (fakta[nokkel] && checkTrue(fakta[nokkel].value)) {
					fakta[nokkel].value = false;
					fakta[nokkel].$save();
				}
			}
		}

	}]);
