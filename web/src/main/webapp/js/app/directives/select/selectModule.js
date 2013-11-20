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

                var input = angular.element(element.find('input'));

                $document.bind('click', function() {
                    avbryt();
                    scope.$apply();
                });

                scope.escape = function() {
                    avbryt();
                }

                scope.klikk = function(event) {
                    event.stopPropagation();
                }

                scope.valgtElement = function(event) {
                    velgListeElement(angular.element(event.target));
                    event.stopPropagation();
                }

                scope.enter = function() {
                    velgListeElement(scope.fokusElement);
                    settFokusTilNesteElement();
                }

                scope.tab = function(event) {
                    velgListeElement(scope.fokusElement);
                    settFokusTilNesteElement();
                    event.preventDefault();
                }

                scope.navigateUp = function() {
                    scrollTil(scope.fokusElement.prevAll(':visible').first());
                }

                scope.navigateDown = function() {
                    scrollTil(scope.fokusElement.nextAll(':visible').first());
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

                    function skalViseListeOverValg() {
                        return input.is(':focus') && scope.inputVerdi && scope.inputVerdi.length > 1;
                    }
                });

                scope.skalViseListen = function() {
                    return hentListeLengde() > 0 && scope.listeErApen;
                }

                scope.filter = function(listeElement, erSisteElement) {
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

                    if (listeElement == scope.fokusElement) {
                        nullstillFokusElement();
                    }

                    if (erSisteElement && scope.listeErApen && scope.fokusElement && scope.fokusElement.not(':visible')) {
                        settFokusElement(forsteIListen());
                        scrollTilFokusElementDersomDetIkkeVises();
                    }

                    return false;
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

                function settFokusTilNesteElement() {
                    var fokuserbareElementer = $('input, a, select, button, textarea').filter(':visible');
                    fokuserbareElementer.eq(fokuserbareElementer.index(input) + 1).focus();
                }

                function hentListeLengde() {
                    return element.find('li:not(.ng-hide)').length;
                }

                function settFokusElement(nyttElement) {
                    scope.fokusElement = nyttElement;
                    scope.fokusElement.addClass('harFokus');

                    if (scope.fokusElement.not(':visible')) {

                    }
                }

                function velgListeElement(item) {
                    if (scope.fokusElement) {
                        scope.ngModel = item.attr('data-value');
                        scope.inputVerdi = hentValgtTekstBasertPaaValue();
                    } else {
                        scope.inputVerdi = '';
                        scope.ngModel = '';
                    }

                    lukk();
                }

                function apne() {
                    if (!scope.listeErApen) {
                        scope.listeErApen = true;
                        if (scope.fokusElement && scope.fokusElement.hasClass('ng-hide')) {
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

                function nullstillFokusElement() {
                    scope.fokusElement.removeClass('harFokus');
                    scope.fokusElement = undefined;
                }

                function lukk() {
                    scope.listeErApen = false;
                    if (scope.fokusElement) {
                        nullstillFokusElement();
                    }
                }

                function scrollTilFokusElementDersomDetIkkeVises() {
                    var diff = visesHeleElementet(scope.fokusElement, scope.fokusElement.parent());
                    if (diff != 0) {
                        scope.fokusElement.parent().scrollToPos(scope.fokusElement.parent().scrollTop() - diff, 0);
                    }
                }

                function byttFokusElement(nyttElement) {
                    if (scope.fokusElement) {
                        scope.fokusElement.removeClass('harFokus');
                    }
                    settFokusElement(nyttElement);
                }

                function scrollTil(elem) {
                    apne();
                    console.log(elem.text());
                    if (elem.length > 0) {
                        byttFokusElement(angular.element(elem));
                        scrollTilFokusElementDersomDetIkkeVises();
                    }
                }

                function forsteIListen() {
                    return angular.element(element.find('li:not(.ng-hide)').first());
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