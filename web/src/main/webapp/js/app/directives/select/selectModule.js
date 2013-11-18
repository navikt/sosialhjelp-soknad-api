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

                scope.listeErApen = false;
                scope.soketekst = '';

                if (scope.ngRequired === undefined) {
                    scope.ngRequired = false;
                }

                if (scope.requiredFeilmelding === undefined) {
                    scope.requiredFeilmelding = 'select.required.feilmelding';
                }

                if (scope.ugyldigFeilmelding === undefined) {
                    scope.ugyldigFeilmelding = 'select.ugyldig.feilmelding';
                }

                var sisteNavigasjonMedPiltaster = new Date().getTime();
                var input = angular.element(element.find('input'));

                $document.bind('click', function() {
                    avbryt();
                    scope.$apply();
                });

                scope.klikk = function(event) {
                    apne();
                    event.stopPropagation();
                }

                scope.selectionChanged = function(event) {
                    velgListeElement(angular.element(event.target));
                    event.stopPropagation();
                }

                scope.mouseover = function(event) {
                    if (event.timeStamp - sisteNavigasjonMedPiltaster > 300) {
                        settFokusElement(angular.element(event.target));
                    }
                }

                scope.enter = function() {
                    if (scope.fokusElement) {
                        velgListeElement(scope.fokusElement);
                    }

                    var fokuserbareElementer = $('input, a, select, button, textarea').filter(':visible');
                    fokuserbareElementer.eq(fokuserbareElementer.index(input) + 1).focus();
                }

                scope.tab = function() {
                    if (scope.fokusElement) {
                        velgListeElement(scope.fokusElement);
                    }
                }

                scope.escape = function() {
                    avbryt();
                }

                scope.navigateUp = function() {
                    scrollTil(scope.fokusElement.prev());
                }

                scope.navigateDown = function() {
                    scrollTil(scope.fokusElement.next());
                }

                scope.$watch('inputVerdi', function() {
                    if (scope.inputVerdi) {
                        scope.inputVerdi = scope.inputVerdi.trim();
                    }
                    scope.soketekst = scope.inputVerdi;
                });

                scope.keypress = function() {
                    apne();
                }

                scope.skalViseListen = function() {
                    return hentListeLengde() > 0 && scope.listeErApen;
                }

                /*
                 * TODO: Fiks stygg hack
                 * Dersom ett element har fokus, men blir filtrert bort, må fokus settes
                 * til første element. Per nå løst med å bruke ng-class for å kjøre denne metoden...
                 */
                scope.fokusHack = function(last) {
                    if (last && elementMedFokus().length == 0) {
                        settFokusElement(forsteIListen());
                    }
                }

                scope.filter = function(listeElement) {
                    var tekst = listeElement.text;

                    if (!scope.soketekst) {
                        listeElement['displayText'] = tekst;
                        return true;
                    }

                    var indeks = stringContainsNotCaseSensitive(tekst, scope.soketekst);
                    if (indeks == 0) {
                        var ikkeMarkertTekst = tekst.substring(indeks + scope.soketekst.length, tekst.length);
                        var markertTekst = tekst.substring(indeks, indeks + scope.soketekst.length);
                        var vistTekst = "<span>" + markertTekst + "</span>" + ikkeMarkertTekst;

                        listeElement['displayText'] = vistTekst;
                        return true;
                    }

                    return false;
                }

                scope.harRequiredFeil = function() {
                    return scope.ngRequired && !scope.inputVerdi && !scope.listeErApen;
                }

                scope.inneholderIkkeSkrevetTekst = function() {
                    return hentListeLengde() == 0 && scope.inputVerdi;
                }

                scope.harFeil = function() {
                    return scope.harRequiredFeil() || scope.inneholderIkkeSkrevetTekst();
                }

                function hentListeLengde() {
                    return element.find('li:not(.ng-hide)').length;
                }

                function settFokusElement(nyttElement) {
                    if (scope.fokusElement) {
                        scope.fokusElement.removeClass('harFokus');
                    }
                    scope.fokusElement = nyttElement;
                    scope.fokusElement.addClass('harFokus');

                    if (scope.fokusElement.not(':visible')) {

                    }
                }

                function velgListeElement(item) {
                    if (hentListeLengde() == 0) {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                    } else {
                        scope.ngModel = item.attr('data-value');
                        scope.inputVerdi = hentValgtTekstBasertPaaValue();
                    }

                    lukk();
                }

                function apne() {
                    if (!scope.listeErApen) {
                        scope.listeErApen = true;
                        scope.soketekst = '';
                        if (scope.fokusElement.hasClass('ng-hide')) {
                            settFokusElement(forsteIListen());
                        }
                    }
                }

                function avbryt() {
                    if (scope.listeErApen) {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                        lukk();
                    }
                }

                function lukk() {
                    scope.listeErApen = false;
                }

                function scrollTil(elem) {
                    apne();
                    if (elem.length > 0) {
                        settFokusElement(angular.element(elem));
                        var diff = visesHeleElementet(elem, elem.parent());
                        if (diff != 0) {
                            elem.parent().scrollToPos(elem.parent().scrollTop() - diff, 0);
                            sisteNavigasjonMedPiltaster = new Date().getTime();
                        }
                    }
                }

                function forsteIListen() {
                    return angular.element(element.find('li:not(.ng-hide)').first());
                }

                function elementMedFokus() {
                    return angular.element(element.find('li.harFokus:not(.ng-hide)'));
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
            }
        }
    }]);