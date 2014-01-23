angular.module('nav.dagpenger', [])
	.controller('DagpengerCtrl', ['$scope', 'data', '$location', '$timeout', function ($scope, data, $location, $timeout) {

		$scope.grupper = [
			{id: 'reellarbeidssoker', tittel: 'reellarbeidssoker.tittel', template: '../html/templates/reellarbeidssoker/reell-arbeidssoker.html', apen: false},
			{id: 'arbeidsforhold', tittel: 'arbeidsforhold.tittel', template: '../html/templates/arbeidsforhold.html', apen: false},
			{id: 'egennaering', tittel: 'ikkeegennaering.tittel', template: '../html/templates/egen-naering.html', apen: false},
			{id: 'verneplikt', tittel: 'ikkeavtjentverneplikt.tittel', template: '../html/templates/verneplikt.html', apen: false},
			{id: 'utdanning', tittel: 'utdanning.tittel', template: '../html/templates/utdanning/utdanning.html', apen: false},
			{id: 'ytelser', tittel: 'ytelser.tittel', template: '../html/templates/ytelser.html', apen: false},
			{id: 'personalia', tittel: 'personalia.tittel', template: '../html/templates/personalia.html', apen: false},
			{id: 'barnetillegg', tittel: 'barnetillegg.tittel', template: '../html/templates/barnetillegg.html', apen: false}
		];

		$scope.validerDagpenger = function (form, event) {
			//burde refaktoreres, bruke noe annet en events?
			event.preventDefault();

			$scope.$broadcast('VALIDER_YTELSER', form.ytelserForm);
			$scope.$broadcast('VALIDER_UTDANNING', form.utdanningForm);
			$scope.$broadcast('VALIDER_ARBEIDSFORHOLD', form.arbeidsforholdForm);
			$scope.$broadcast('VALIDER_EGENNAERING', form.egennaeringForm);
			$scope.$broadcast('VALIDER_VERNEPLIKT', form.vernepliktForm);
			$scope.$broadcast('VALIDER_REELLARBEIDSSOKER', form.reellarbeidssokerForm);
			$scope.$broadcast('VALIDER_DAGPENGER', form);

			$timeout(function () {
				$scope.validateForm(form.$invalid);
				var elementMedForsteFeil = $('.accordion-group').find('.form-linje.feil, .form-linje.feilstyling').first();
				if (form.$valid) {
					$location.path('/vedlegg');

				} else {
					scrollToElement(elementMedForsteFeil, 200);
					giFokus(elementMedForsteFeil);
					setAktivFeilmeldingsklasse(elementMedForsteFeil);
				}
			}, 400);
		};

		$scope.$on('OPEN_TAB', function (e, ider) {
			settApenStatusForAccordion(true, ider);
		});

		$scope.$on('CLOSE_TAB', function (e, ider) {
			settApenStatusForAccordion(false, ider);
		});

		function settApenStatusForAccordion(apen, ider) {
			if (ider instanceof Array) {
				angular.forEach(ider, function (id) {
					settApenForId(apen, id);
				});
			} else {
				settApenForId(apen, ider);
			}
		}

		function settApenForId(apen, id) {
			var idx = $scope.grupper.indexByValue(id);
			if (idx > -1) {
				$scope.grupper[idx].apen = apen;
			}
		}

		function giFokus(element) {
			element.find(':input').focus();
		}

		function setAktivFeilmeldingsklasse(element) {
			element.addClass('aktiv-feilmelding');
		}
	}]);
