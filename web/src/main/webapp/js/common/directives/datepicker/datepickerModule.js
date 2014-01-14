/**
 * Både direktiv for å legge til enkel datepicker og til-fra dato.
 *
 * nav-dato: Legger til en enkel datepicker. Følgende attributter må oppgis:
 *      - ngModel: Modellen hvor valgt dato skal lagres
 *      - label: Nøkkel til CMS for å hente ut label-tekst
 *
 * Følgende attributter kan oppgis:
 *      - er-required: Expression som sier om feltet er påkrevd eller ikke
 *      - required-error-message: Nøkkel til CMS for å hente ut feilmeldingstekst for required-feil
 *      - er-fremtidigdato-tilatt: Expression som sier om det er lovelig å sette datoen frem i tid.
 *
 * Følgende attributter brukes av det andre direktivet for å kjøre validering på datointervallet:
 *      - fraDato: Startdato i intervallet
 *      - tilDato: Sluttdato i intervallet
 *      - tilDatoFeil: Boolean som sier om vi har hatt en feil hvor sluttdato er før startdato
 *
 * Disse attributtene skal man aldri sette når man bruke nav-dato.
 *
 *
 * nav-dato-intervall: Legger til en datepicker med start- og sluttdato. Følgende attributter må oppgis:
 *      - fraDato: Startdato i intervallet
 *      - tilDato: Sluttdato i intervallet
 *      - label: Starten på nøkkeler i CMS. Bruker til å hente ut label og required-feilmelding for begge datepickere.
 *               Dersom label er 'datepicker.noe', må følgende nøkler ligge i CMS:
 *                  - datepicker.noe.til=Label til startdato
 *                  - datepicker.noe.fra=Label til sluttdato
 *                  - datepicker.noe.til.feilmelding=Required-feilmelding for startdato
 *                  - datepicker.noe.fra.feilmelding=Required-feilmelding for sluttdato
 *
 * Følgende attributter kan oppgis:
 *      - er-tildato-required: Expression som sier om til-dato er påkrevd. Er false dersom ikke oppgitt
 *      - er-fradato-required: Expression som sier om fra-dato er påkrevd. Er false dersom ikke oppgitt
 *      - er-begge-required: Expression som sier om både til- og fra-dato er påkrevd. Er false dersom ikke oppgitt.
 *                           Denne setter både er-fradato-required og er-tildato-required.
 *      - er-fremtidigdato-tillatt: Expression som sier om det er lovelig å sette datoen frem i tid.
 */

angular.module('nav.datepicker', [])
	.constant('datepickerConfig', {
		altFormat  : 'dd.MM.yyyy',
		dateFormat : 'dd.mm.yy',
		changeMonth: true,
		changeYear : true
	})
	.directive('navDato', ['$timeout', 'datepickerConfig', '$filter', function ($timeout, datepickerConfig, $filter) {
		return {
			restrict   : 'A',
			require    : '^form',
			replace    : true,
			templateUrl: '../js/common/directives/datepicker/singleDatepickerTemplate.html',
			scope      : {
				ngModel               : '=',
				erRequired            : '=',
				tilDato               : '=',
				fraDato               : '=',
				tilDatoFeil           : '=',
				erFremtidigdatoTillatt: '=',
				endret                : '&',
				lagre                 : '&',
				label                 : '@',
				requiredErrorMessage  : '@'
			},
			link       : function (scope, element, attrs, form) {
				var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
				var datoRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);
				var tekstInput = element.find('input').first();
				var datepickerInput = element.find('input').last();
				var harHattFokus = false;
				var datepickerErLukket = true;
				scope.harFokus = false;

				scope.options = {};
				if (!scope.erFremtidigdatoTillatt) {
					scope.options['maxDate'] = new Date();
				}

				scope.toggleDatepicker = function () {
					var dateDiv = $('#ui-datepicker-div');
					if (dateDiv.is(':hidden')) {
						datepickerInput.datepicker('show');
						var pos = dateDiv.position();
						pos.top = pos.top + 32;
						dateDiv.offset(pos);
					}
				};

				scope.blur = function () {
					scope.harFokus = false;

					if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
						scope.ngModel = '';
						scope.tilDatoFeil = true;
					} else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
						scope.tilDato = '';
						scope.tilDatoFeil = true;
					}

					if (scope.lagre) {
						$timeout(scope.lagre, 100);
					}
				};

				scope.focus = function () {
					scope.harFokus = true;
					harHattFokus = true;
					if (scope.tilDatoFeil !== undefined) {
						scope.tilDatoFeil = false;
					}
				};

				scope.$on(eventForAValidereHeleFormen, function () {
					harHattFokus = true;
				});

				scope.harRequiredFeil = function () {
					return scope.erRequired && !scope.ngModel && !scope.harFokus && harHattFokus && datepickerErLukket &&
						!scope.tilDatoFeil && !inputfeltHarTekstMenIkkeGyldigDatoFormat() && !erGyldigDato(tekstInput.val());
				};

				scope.harTilDatoFeil = function () {
					return !scope.ngModel && !scope.harFokus && harHattFokus && datepickerErLukket && scope.tilDatoFeil;
				};

				scope.harFormatteringsFeil = function () {
					return inputfeltHarTekstMenIkkeGyldigDatoFormat() && !scope.harFokus && harHattFokus;
				};

				scope.erIkkeGyldigDato = function () {
					return !scope.ngModel && inputfeltHarTekstOgGyldigDatoFormat() &&
						!erGyldigDato(tekstInput.val()) && !scope.harFokus && harHattFokus;
				};

				scope.harFeil = function () {
					return scope.harRequiredFeil() || scope.harFormatteringsFeil() || scope.harTilDatoFeil() || scope.erIkkeGyldigDato();
				};

				scope.$watch('ngModel', function (newVal, oldVal) {
					if (newVal === oldVal) {
						return;
					}

					if (newVal !== oldVal && scope.endret) {
						scope.endret();
					}

					if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
						scope.ngModel = '';
						scope.tilDatoFeil = true;
					} else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
						scope.tilDato = '';
						scope.tilDatoFeil = true;
					}

					if (isNaN(new Date(scope.ngModel).getDate())) {
						datepickerInput.datepicker('setDate', new Date());
					} else {
						datepickerInput.datepicker('setDate', new Date(scope.ngModel));
					}
				});

				function inputfeltHarTekstOgGyldigDatoFormat() {
					return tekstInput.val() && datoRegExp.test(tekstInput.val());
				}

				function inputfeltHarTekstMenIkkeGyldigDatoFormat() {
					return tekstInput.val() && !datoRegExp.test(tekstInput.val());
				}

				var defaultDate = new Date();

				function datepickerOptions() {
					var currentDefaultDate = defaultDate;
					defaultDate = scope.ngModel ? new Date(scope.ngModel) : currentDefaultDate;

					if (currentDefaultDate.getTime() !== defaultDate.getTime()) {
						scope.options = angular.extend({}, {defaultDate: defaultDate}, scope.options);
					}
					return angular.extend({}, datepickerConfig, scope.options);
				}

				function leggTilDatepicker() {
					var opts = datepickerOptions();

					opts.onSelect = function () {
						var dato = datepickerInput.datepicker('getDate');
						scope.ngModel = $filter('date')(dato, 'yyyy-MM-dd');
					};

					opts.beforeShow = function () {
						datepickerErLukket = false;
						harHattFokus = true;
					};
					opts.onClose = function () {
						datepickerErLukket = true;
						scope.$apply();
					};

					datepickerInput.datepicker('destroy');
					datepickerInput.datepicker(opts);
				}
				// Legger til datepicker på nytt dersom options endrer seg
				scope.$watch(datepickerOptions, leggTilDatepicker, true);
			}
		}
	}])
	.directive('navDatoIntervall', [function () {
		return {
			restrict   : 'A',
			replace    : true,
			templateUrl: '../js/common/directives/datepicker/doubleDatepickerTemplate.html',
			scope      : {
				fraDato               : '=',
				tilDato               : '=',
				erFradatoRequired     : '=',
				erTildatoRequired     : '=',
				erBeggeRequired       : '=',
				erFremtidigdatoTillatt: '=',
				label                 : '@'
			},
			controller : function ($scope) {
				$scope.fraLabel = $scope.label + '.fra';
				$scope.tilLabel = $scope.label + '.til';
				$scope.fraFeilmelding = $scope.fraLabel + '.feilmelding';
				$scope.tilFeilmelding = $scope.tilLabel + '.feilmelding';
				$scope.tilDatoFeil = false;

				if ($scope.erBeggeRequired) {
					$scope.$watch('erBeggeRequired', function () {
						$scope.fradatoRequired = $scope.erBeggeRequired;
						$scope.tildatoRequired = $scope.erBeggeRequired;
					});
				} else {
					$scope.$watch('erFradatoRequired', function () {
						$scope.fradatoRequired = $scope.erFradatoRequired;
					});

					$scope.$watch('erTildatoRequired', function () {
						$scope.tildatoRequired = $scope.erTildatoRequired;
					});
				}
			}
		}
	}])
	.directive('datoMask', ['$filter', 'cms', function ($filter, cms) {
		return {
			restrict: 'A',
			require : 'ngModel',
			link    : function (scope, element, attrs, ngModel) {
				if (!ngModel) {
					return;
				}

				var datoMask = cms.tekster['dato.format'];
				var caretPosisjonElement = element.closest('.datepicker').find('.caretPosition');
				var maskElement = element.next();
				var inputElementVenstre = element.position().left + 7;
				var topp = getTopp();
				var venstre = inputElementVenstre;
				maskElement.css({top: topp + 'px', left: venstre + 'px'});
				maskElement.text(datoMask);
				caretPosisjonElement.hide();

				element.bind('blur', function () {
					caretPosisjonElement.hide();
				});

				element.bind('focus', function () {
					caretPosisjonElement.show();
				});

				element.bind('keydown', function (event) {
					return event.keyCode !== 32;
				});

				ngModel.$formatters.unshift(function (dato) {
					if (dato) {
						var datoSomDateObjekt = new Date(dato);
						return $filter('date')(datoSomDateObjekt, 'dd.MM.yyyy');
					} else {
						return '';
					}
				});

				var gammelInputVerdi = '';
				ngModel.$parsers.unshift(function (datoInput) {
					var slettet = datoInput.length < gammelInputVerdi.length;
					var caretPosisjon = hentCaretPosisjon(element);

					if (!slettet) {
						var start = caretPosisjon - (datoInput.length - gammelInputVerdi.length);
						var slutt = caretPosisjon;

						for (var i = start; i < slutt && i < datoInput.length; i++) {
							var skrevetTegn = datoInput[i];

							if (isNaN(skrevetTegn) || datoInput.substring(0, i + 1).length > datoMask.length || datoInput.splice(i, 1, '').length == datoMask.length) {
								if (skrevetTegn !== '.' || (i !== 3 && i !== 5)) {
									datoInput = datoInput.splice(i, 1, '');
									caretPosisjon--;
									i--;
									slutt--;
									continue;
								}
							}

							if (i === 1 || i === 4) {
								if (datoInput[i + 1] === '.') {
									caretPosisjon++;
									i++;
								} else {
									datoInput = datoInput.splice(i + 1, 0, '.');
									caretPosisjon++;
									i++;
									slutt++;
								}
							}
						}
					}

					gammelInputVerdi = datoInput;
					ngModel.$viewValue = datoInput;
					ngModel.$render();
					settCaretPosisjon(element, caretPosisjon);

					return reverserNorskDatoformat(datoInput);
				});

				scope.$watch(
					function () {
						return ngModel.$viewValue;
					},
					function (nyVerdi) {
						var tekst = nyVerdi;
						if (nyVerdi === undefined) {
							tekst = '';
						}

						gammelInputVerdi = tekst;
						caretPosisjonElement.text(tekst);
						venstre = inputElementVenstre + caretPosisjonElement.outerWidth();
						maskElement.css({top: getTopp() + 'px', left: venstre + 'px'});

						var antallPunktum = tekst.match(/\./g) === null ? 0 : tekst.match(/\./g).length;
						var maskTekst = '';
						var maanedTekst = tekst.substring(tekst.indexOf('.'), tekst.length);
						var aarTekst = tekst.substring(tekst.lastIndexOf('.'), tekst.length);

						if (skalViseDatoFormatFraOgMedDag()) {
							maskTekst = datoMask.substring(tekst.length, datoMask.length);
						} else if (skalViseDatoFormatFraOgMedMaaned()) {
							maskTekst = datoMask.substring(2 + maanedTekst.length, datoMask.length);
						} else if (skalBareViseDatoFormatMedAar()) {
							maskTekst = datoMask.substring(5 + aarTekst.length, datoMask.length);
						}
						maskElement.text(maskTekst);

						function skalViseDatoFormatFraOgMedDag() {
							return antallPunktum === 0 && tekst.length < 3;
						}

						function skalViseDatoFormatFraOgMedMaaned() {
							var dagTekst = tekst.substring(0, tekst.indexOf('.'));
							return antallPunktum === 1 && dagTekst.length < 3 && maanedTekst.length < 4;
						}

						function skalBareViseDatoFormatMedAar() {
							var dagOgMaanedTekst = tekst.substring(0, tekst.lastIndexOf('.'));
							return antallPunktum === 2 && dagOgMaanedTekst.length < 6 && aarTekst.length < 5;
						}
					}
				);

				function getTopp() {
					return element.position().top + 6;
				}
			}
		}
	}]);
