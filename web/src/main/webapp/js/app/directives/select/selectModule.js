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
    .directive('navSelect', ['$document', '$filter', 'data', '$timeout', function ($document, $filter, data, $timeout) {
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
                scope.orginalListe = scope.$parent.$eval(attrs.options);

                for (var i = 0; i < scope.orginalListe.length; i++) {
                    scope.orginalListe[i]['displayText'] = scope.orginalListe[i].text;
                }

                scope.vistListe = filterListePaaSoketekst();
                scope.vistListeFiltrert = filterListeTilAntallElementerRundtValgtElement();
                scope.defaultValue = data.tekster[attrs.defaultValue] ? data.tekster[attrs.defaultValue] : attrs.defaultValue;
                scope.ngModel = (scope.defaultValue) ? scope.defaultValue : '';
                scope.inputVerdi = hentValgtTekstBasertPaaValue();
                scope.valgtElementVerdi = scope.ngModel;

                scope.listeErApen = false;
                scope.soketekst = '';

                var antallElementerOverOgUnder = 30;
                var minimumIndeks;
                var maximumIndeks;

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
                var liste = angular.element(element.find('ul'));

                element.find('ul').bind('scroll', _.throttle(leggTilElementVedScroll, 200));

                function leggTilElementVedScroll() {
                    erScrolletNestenHeltOpp();
                    erScrolletNestenHeltNed();
                }

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
                    scope.soketekst = '';
                    filtrerListe();
                    apne();
                    $timeout(function() {
                        scrollSlikAtElementMedFokusErPaaToppenDersomDetIkkeVises();
                    });
                    event.stopPropagation();
                }

                scope.escape = function() {
                    avbryt();
                }

                scope.klikk = function(event) {
                    event.stopPropagation();
                }

                scope.valgtElement = function(event, verdi) {
                    scope.valgtElementVerdi = verdi;
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
                    var forrige = hentListeelementMedVerdi(scope.valgtElementVerdi).prevAll(':visible');
                    if (forrige.length > 0) {
                        scope.valgtElementVerdi = forrige.attr('data-value');
                        scrollTilElementMedFokus();
                    }
                    event.preventDefault();
                }

                scope.navigateDown = function(event) {
                    apne();
                    var neste = hentListeelementMedVerdi(scope.valgtElementVerdi).nextAll(':visible');
                    if (neste.length > 0) {
                        scope.valgtElementVerdi = neste.attr('data-value');
                        scrollTilElementMedFokus();
                    }
                    event.preventDefault();
                }

                function filtrerListe() {
                    scope.vistListe = filterListePaaSoketekst();
                    scope.vistListeFiltrert = filterListeTilAntallElementerRundtValgtElement();
                }

                scope.$watch('inputVerdi', function() {
                    if (scope.inputVerdi) {
                        scope.inputVerdi = scope.inputVerdi.trim();
                    }
                    scope.soketekst = scope.inputVerdi;
                    if (skalViseListeOverValg()) {
                        apne();
                    } else {
                        lukk();
                    }

                    filtrerListe();

                    $timeout(function() {
                        scrollSlikAtElementMedFokusErPaaToppenDersomDetIkkeVises();
                    });

                    function skalViseListeOverValg() {
                        return scope.listeErApen || (input.is(':focus') && scope.inputVerdi && scope.inputVerdi.length > 1);
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

                scope.harFokus = function(verdi) {
                    var valgtElementErSynlig = scope.valgtElementVerdi && erListeelementMedGittVerdiSynlig(scope.valgtElementVerdi);

                    if (valgtElementErSynlig && scope.valgtElementVerdi == verdi) {
                        return true;
                    } else if (!valgtElementErSynlig && hentVerdiTilForsteSynligeListeelement() == verdi) {
                        scope.valgtElementVerdi = verdi;
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
                    if (scope.valgtElementVerdi.trim()) {
                        scope.ngModel = scope.valgtElementVerdi;
                        scope.inputVerdi = hentValgtTekstBasertPaaValue();
                    } else {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                    }

                    lukk();
                }

                function apne() {
                    if (!scope.listeErApen) {
                        scope.valgtElementVerdi = scope.ngModel;
                        scope.listeErApen = true;
                    }
                }

                function lukk() {
                    scope.listeErApen = false;
                }

                function avbryt() {
                    if (scope.listeErApen) {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                        scope.valgtElementVerdi = '';
                        lukk();
                    }
                }

                function scrollSlikAtElementMedFokusErPaaToppenDersomDetIkkeVises() {
                    if (!scope.valgtElementVerdi) {
                        return;
                    }

                    var fokusElement = hentListeelementMedVerdi(scope.valgtElementVerdi);
                    var diff = visesHeleElementet(fokusElement, fokusElement.parent());
                    var offset = 25; // Må trekkes fra for at elementet skal komme øverst.

                    if (diff > 0) {
                        fokusElement.parent().scrollToPos(fokusElement.parent().scrollTop() - diff, 0);
                    } else if (diff < 0) {
                        fokusElement.parent().scrollToPos(fokusElement.parent().scrollTop() - diff + fokusElement.parent().height() - offset, 0);
                    }
                }

                function scrollTilElementMedFokus() {
                    var fokusElement = hentListeelementMedVerdi(scope.valgtElementVerdi);
                    var diff = visesHeleElementet(fokusElement, fokusElement.parent());
                    if (diff != 0) {
                        fokusElement.parent().scrollToPos(fokusElement.parent().scrollTop() - diff, 0);
                    }
                }

                function hentVerdiTilForsteSynligeListeelement() {
                    return element.find('li:visible').first().attr('data-value');
                }

                function erListeelementMedGittVerdiSynlig(verdi) {
                    return hentListeelementMedVerdi(verdi).is(':visible');
                }

                function hentListeelementMedVerdi(verdi) {
                    return angular.element(element.find('li[data-value=' + verdi + ']'));
                }

                function hentValgtTekstBasertPaaValue() {
                    var idx = scope.orginalListe.indexByValue(scope.ngModel);

                    if (idx > -1) {
                        return scope.orginalListe[idx].text;
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

                function erScrolletNestenHeltOpp() {
                    if (liste[0].scrollTop < 200) {
                        var antallNyeElementer = (200 - liste[0].scrollTop)/10 + 10;
                        var gammelMinIndeks = minimumIndeks;
                        minimumIndeks = Math.max(0, minimumIndeks - antallNyeElementer);
                        scope.vistListeFiltrert = scope.vistListe.slice(minimumIndeks, gammelMinIndeks).concat(scope.vistListeFiltrert);
                        scope.$apply();
                    }
                };

                function erScrolletNestenHeltNed() {
                    var scrolled = liste[0].scrollHeight - (liste[0].offsetHeight + liste[0].scrollTop);

                    if (scrolled < 200) {
                        var antallNyeElementer = (200 - scrolled)/10 + 10;
                        var gammelMaxIndeks = maximumIndeks;
                        maximumIndeks = Math.min(scope.vistListe.length, maximumIndeks + antallNyeElementer);
                        scope.vistListeFiltrert = scope.vistListeFiltrert.concat(scope.vistListe.slice(gammelMaxIndeks, maximumIndeks));
                        scope.$apply();
                    }
                };

                function filterListePaaSoketekst() {
                    var input = scope.orginalListe;
                    var query = scope.soketekst;

                    if (query) {
                        query = query.trim();
                    } else {
                        query = '';
                    }

                    if (!query) {
                        return input;
                    }

                    var matchArray = [];
                    for (var i = 0; i < input.length; i++) {
                        var text = input[i].text;
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

                function filterListeTilAntallElementerRundtValgtElement() {
                    console.log(scope.valgtElementVerdi);
                    var valgtIndeks = scope.vistListe.indexByValue(scope.valgtElementVerdi);
                    console.log(valgtIndeks);

                    minimumIndeks = Math.max(0, valgtIndeks - antallElementerOverOgUnder);
                    maximumIndeks = valgtIndeks + antallElementerOverOgUnder + Math.max(0, antallElementerOverOgUnder - valgtIndeks);
                    var nyListe = scope.vistListe.slice(minimumIndeks, maximumIndeks);

                    return nyListe;
                }
            }
        }
    }]);