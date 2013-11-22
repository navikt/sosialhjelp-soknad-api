/**
 * Legger til søkbar select-boks.
 *
 * Følgende konfigurering er mulig:
 * label: String med nøkkel til CMS for label til selectboksen
 *
 * ng-model: Modellen hvor valget vil bli lagret
 *
 * options: Valgene i selectboksen. Dette må være en liste med objekt, hvor hvert objekt inneholder to nøkler, text og value.
 * Text er teksten som vises i listen, mens value er verdien som lagres i modellen. Kan legges på filter for å filtrere listen.
 *
 * default-value: String som sier om det skal være et element valgt som default. Dette kan være en nøkkel til CMS, hvor verdien i CMS må
 * matche 'value' til objektet som skal være default. Det kan også være en string som matcher 'value' til objektet som er default. Dersom ingen
 * verdi er gitt, vil det ikke være en default verdi.
 *
 * ng-required: Samme som standard angular required. False dersom ikke oppgitt.
 *
 * required-feilmelding: Nøkkel til feilmelding som skal vises dersom feltet er required men ikke fylt inn. Dersom ikke oppgitt blir det en standard nøkkel.
 *
 * ugyldig-feilmelding: Nøkkel til feilmelding som skal vises dersom man skriver inn noe som ikke finnes i listen. Dersom ikke oppgitt blir det en standard nøkkel.
 */
angular.module('nav.select', ['ngSanitize'])
    .directive('navSelect', ['$document', '$filter', 'data', function ($document, $filter, data) {
        return {
            require: 'ngModel',
            scope: {
                requiredFeilmelding: '@',
                ugyldigFeilmelding: '@',
                label: '@',
                ngRequired: '=',
                ngModel: '='
            },
            replace: true,
            templateUrl: '../js/app/directives/select/selectTemplate.html',
            link: function (scope, element, attrs) {
                scope.options = scope.$parent.$eval(attrs.options);
                scope.defaultValue = data.tekster[attrs.defaultValue] ? data.tekster[attrs.defaultValue] : attrs.defaultValue;
                scope.ngModel = (scope.defaultValue) ? scope.defaultValue : '';
                scope.inputVerdi = hentValgtTekstBasertPaaValue();
                scope.valgtElementIndeks = -1;

                scope.listeErApen = false;
                scope.soketekst = '';
                scope.visAntallListeElement = 30;

                if (scope.ngRequired === undefined) {
                    scope.ngRequired = false;
                }

                if (scope.requiredFeilmelding === undefined) {
                    scope.requiredFeilmelding = 'select.required.feilmelding';
                }

                if (scope.ugyldigFeilmelding === undefined) {
                    scope.ugyldigFeilmelding = 'select.ugyldig.feilmelding';
                }

                var input = angular.element(element.find('input'));

                element.find('ul').bind('scroll', function() {
                    erScrolletNestenHeltNed(this);
                });

                input.focusin(function() {
                    element.addClass("fokus");
                });

                input.focusout(function() {
                    element.removeClass("fokus");
                })

                $document.bind('click', function() {
                    avbryt();
                    scope.$apply();
                });

                scope.apneSelectboks = function(event) {
                    apne();
                    event.stopPropagation();
                }

                scope.escape = function() {
                    avbryt();
                }

                scope.klikk = function(event) {
                    event.stopPropagation();
                }

                scope.valgtElement = function(event, indeks) {
                    scope.valgtElementIndeks = indeks;
                    velgElement();
                    event.stopPropagation();
                }

                scope.enter = function() {
                    velgElement();
                    settFokusTilNesteElement();
                }

                scope.tab = function(event) {
                    velgElement();
                    settFokusTilNesteElement();
                    event.preventDefault();
                }

                scope.navigateUp = function(event) {
                    apne();
                    var forrige = hentListeElementVedIndeks(scope.valgtElementIndeks).prevAll(':visible').index();
                    if (forrige > -1) {
                        scope.valgtElementIndeks = forrige;
                        scrollTilElementMedFokus();
                    }
                    event.preventDefault();
                }

                scope.navigateDown = function(event) {
                    apne();
                    var neste = hentListeElementVedIndeks(scope.valgtElementIndeks).nextAll(':visible').index();
                    if (neste > -1) {
                        scope.valgtElementIndeks = neste;
                        scrollTilElementMedFokus();
                    }
                    event.preventDefault();
                }

                scope.$watch('inputVerdi', function(nyVerdi, gammelVerdi) {
                    if (nyVerdi === gammelVerdi) {
                        return;
                    }

                    if (scope.inputVerdi) {
                        scope.inputVerdi = scope.inputVerdi.trim();
                    }
                    scope.soketekst = scope.inputVerdi;
                    scope.visAntallListeElement = 30;
                    if (skalViseListeOverValg()) {
                        apne();
                    } else {
                        lukk();
                    }

                    function skalViseListeOverValg() {
                        return input.is(':focus') && scope.inputVerdi && scope.inputVerdi.length > 1;
                    }
                });

                scope.skalViseListen = function() {
                    return hentListeLengde() > 0 && scope.listeErApen;
                }

                scope.harRequiredFeil = function() {
                    return scope.ngRequired && !scope.inputVerdi && !scope.listeErApen && !input.is(':focus');
                }

                scope.inneholderIkkeSkrevetTekst = function() {
                    return hentListeLengde() == 0 && scope.inputVerdi;
                }

                scope.harFeil = function() {
                    return scope.harRequiredFeil() || scope.inneholderIkkeSkrevetTekst();
                }

                scope.harFokus = function(listeElement, idx) {
                    var valgtElementVises = scope.valgtElementIndeks > 0 && erListeElementVedIndeksSynlig(scope.valgtElementIndeks);

                    if (valgtElementVises && scope.valgtElementIndeks == idx) {
                        return true;
                    } else if (!valgtElementVises && hentIndeksTilForsteSynligeListeelement() == idx) {
                        scope.valgtElementIndeks = idx;
                        return true
                    } else {
                        return false;
                    }
                }

                function settFokusTilNesteElement() {
                    var fokuserbareElementer = $('input, a, select, button, textarea').filter(':visible');
                    fokuserbareElementer.eq(fokuserbareElementer.index(input) + 1).focus();
                }

                function hentListeLengde() {
                    return element.find('li:not(.ng-hide)').length;
                }

                function velgElement() {
                    if (scope.valgtElementIndeks > -1) {
                        var valgtElement = hentListeElementVedIndeks(scope.valgtElementIndeks);
                        scope.ngModel = valgtElement.attr('data-value');
                        scope.inputVerdi = hentValgtTekstBasertPaaValue();
                    } else {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                    }

                    lukk();
                }

                function apne() {
                    scope.listeErApen = true;
                }

                function lukk() {
                    scope.listeErApen = false;
                    scope.valgtElementIndeks = -1;
                }

                function avbryt() {
                    if (scope.listeErApen) {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                        lukk();
                    }
                }

                function scrollTilElementMedFokus() {
                    var fokusElement = hentListeElementVedIndeks(scope.valgtElementIndeks);
                    var diff = visesHeleElementet(fokusElement, fokusElement.parent());
                    if (diff != 0) {
                        fokusElement.parent().scrollToPos(fokusElement.parent().scrollTop() - diff, 0);
                    }
                }

                function hentIndeksTilForsteSynligeListeelement() {
                    return element.find('li:visible').first().index();
                }

                function erListeElementVedIndeksSynlig(idx) {
                    return hentListeElementVedIndeks(idx).is(':visible');
                }

                function hentListeElementVedIndeks(idx) {
                    return angular.element(element.find('li').get(idx));
                }

                function hentValgtTekstBasertPaaValue() {
                    var idx = scope.options.indexByValue(scope.ngModel);

                    if (idx > -1) {
                        return scope.options[idx].text;
                    }
                    return '';
                }

                function visesHeleElementet(element, container) {
                    var ekstraOffset = 5;

                    var containerTop = container.offset().top;
                    var containerBottom = containerTop + container.height();

                    var elemTop = element.offset().top;
                    var elemBottom = elemTop + element.height();

                    var diffTop = containerTop - elemTop; // Hvis > 0, må scrolle opp
                    var diffBottom = containerBottom - elemBottom; // Hvis < 0 må scrolle ned

                    if (diffTop > 0) {
                        return diffTop + ekstraOffset; // Hvor langt som skal scrolles opp
                    } else if (diffBottom < 0) {
                        return diffBottom - ekstraOffset;  // Hvor langt som skal scrolles ned
                    } else {
                        return 0;
                    }
                }

                function erScrolletNestenHeltNed(elem) {
                    var scrolled = elem.scrollHeight - (elem.offsetHeight + elem.scrollTop);

                    if (scrolled < 150) {
                        scope.visAntallListeElement = Math.min(scope.options.length, scope.visAntallListeElement + 10);
                        console.log(scope.visAntallListeElement);
                        scope.$apply();
                    }
                };
            }
        }
    }])
    .filter('filterSearch', [function () {
        return function (input, query) {
            if (query) {
                query = query.trim();
            } else {
                query = '';
            }

            var matchArray = [];
            for (var i = 0; i < input.length; i++) {
                var text = input[i].text;

                if (!query) {
                    input[i]['displayText'] = text;
                    matchArray.push(input[i]);
                    continue;
                }

                var idx = stringContainsNotCaseSensitive(text, query);
                if (idx == 0) {
                    var endOfString = text.substring(idx + query.length, text.length);
                    var matchedString = text.substring(idx, idx + query.length);
                    var displayText = "<span>" + matchedString + "</span>" + endOfString;
                    input[i]['displayText'] = displayText;
                    matchArray.push(input[i]);
                }
            }
            return matchArray;
        }
    }])
    .filter('filterElement', [function () {
        return function (input, query) {
            var inputElement = input[0];

            if (query) {
                query = query.trim();
            } else {
                query = '';
            }

            if (!query) {
                inputElement['displayText'] = inputElement.text;
                return true;
            }

            var idx = stringContainsNotCaseSensitive(inputElement.text, query);
            if (idx == 0) {
                var endOfString = inputElement.text.substring(idx + query.length, inputElement.text.length);
                var matchedString = inputElement.text.substring(idx, idx + query.length);
                var displayText = "<span>" + matchedString + "</span>" + endOfString;
                inputElement['displayText'] = displayText;
                return true;
            }
            return false;
        }
    }]);
