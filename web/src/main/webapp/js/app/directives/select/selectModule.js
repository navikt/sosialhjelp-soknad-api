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
                scope.selectOpen = false;

                scope.defaultValue = data.tekster[attrs.defaultValue] ? data.tekster[attrs.defaultValue] : attrs.defaultValue;

                scope.ngModel = (scope.defaultValue) ? scope.defaultValue : '';
                scope.inputValue = hentValgtTekstBasertPaaValue();
                scope.searchFilter = '';

                if (scope.ngRequired === undefined) {
                    scope.ngRequired = false;
                }

                var arrowNavigationTimestamp = new Date().getTime();
                var input = angular.element(element.find('input'));

                if (scope.requiredFeilmelding === undefined) {
                    scope.requiredFeilmelding = 'select.required.feilmelding';
                }

                if (scope.ugyldigFeilmelding === undefined) {
                    scope.ugyldigFeilmelding = 'select.ugyldig.feilmelding';
                }

                $document.bind('click', function() {
                    avbryt();
                    scope.$apply();
                });

                scope.klikk = function(event) {
                    console.log(event);
                    apne();
                    event.stopPropagation();
                }

                scope.selectionChanged = function(event) {
                    console.log("Valgt nytt element");
                    selectItem(angular.element(event.target));
                    event.stopPropagation();
                }

                scope.mouseover = function(event) {
                    if (event.timeStamp - arrowNavigationTimestamp > 300) {
                        settFokusElement(angular.element(event.target));
                    }
                }

                scope.enter = function(event) {
                    if (scope.fokusElement) {
                        selectItem(scope.fokusElement);
                    }

                    var focusable = $('input, a, select, button, textarea').filter(':visible');
                    focusable.eq(focusable.index(input) + 1).focus();
                }

                scope.tab = function() {
                    if (scope.fokusElement) {
                        selectItem(scope.fokusElement);
                    }
                }

                scope.escape = function() {
                    avbryt();
                }

                scope.navigateUp = function() {
                    apne();
                    var previous = scope.fokusElement.prev();
                    navigateTo(previous);
                }

                scope.navigateDown = function() {
                    apne();
                    var next = scope.fokusElement.next();
                    navigateTo(next);
                }

                scope.$watch('inputValue', function() {
                    if (scope.inputValue) {
                        scope.inputValue = scope.inputValue.trim();
                    }
                    scope.searchFilter = scope.inputValue;
                });

                scope.keypress = function() {
                    apne();
                }

                scope.skalViseListen = function() {
                    var listeLengde = hentListeLengde();
                    if (listeLengde == 0 && scope.inputValue) {
                        element.addClass('feil');
                        element.find('.melding').text(data.tekster[scope.ugyldigFeilmelding]);
                    } else if (!scope.ngRequired || scope.inputValue) {
                        element.removeClass('feil');
                    }
                    return listeLengde > 0 && scope.selectOpen;
                }

                /*
                 * TODO: Fiks stygg hack
                 * Dersom ett element har fokus, men blir filtrert bort, må fokus settes
                 * til første element. Per nå løst med å bruke ng-class for å kjøre denne metoden...
                 */
                scope.fokusHack = function(last) {
                    if (last && fokusItem().length == 0) {
                        settFokusElement(firstInList());
                    }
                }
                scope.filter = function(item) {
                    var text = item.text;

                    if (!scope.searchFilter) {
                        item['displayText'] = text;
                        return true;
                    }

                    var idx = stringContainsNotCaseSensitive(text, scope.searchFilter);
                    if (idx == 0) {
                        var endOfString = text.substring(idx + scope.searchFilter.length, text.length);
                        var matchedString = text.substring(idx, idx + scope.searchFilter.length);
                        var displayText = "<span>" + matchedString + "</span>" + endOfString;
                        item['displayText'] = displayText;
                        return true;
                    }

                    return false;
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

                function selectItem(item) {
                    if (hentListeLengde() == 0) {
                        scope.inputValue = '';
                        scope.ngModel = '';
                    } else {
                        scope.ngModel = item.attr('data-value');
                        scope.inputValue = hentValgtTekstBasertPaaValue();
                        console.log(scope.ngModel);
                    }

                    lukk();
                }

                function apne() {
                    if (!scope.selectOpen) {
                        scope.selectOpen = true;
                        scope.searchFilter = '';
                        if (scope.fokusElement === undefined) {
                            settFokusElement(firstInList());
                        }
                        element.removeClass('feil');
                    }
                }

                function avbryt() {
                    if (scope.selectOpen) {
                        scope.inputValue = '';
                        scope.ngModel = '';
                        lukk();
                    }
                }

                function lukk() {
                    scope.selectOpen = false;
                    console.log(scope.selectOpen);

                    if (scope.ngRequired && !scope.inputValue) {
                        element.addClass('feil');
                        element.find('.melding').text(data.tekster[scope.requiredFeilmelding]);
                    }
                }

                function navigateTo(elem) {
                    if (elem.length > 0) {
                        settFokusElement(angular.element(elem));
                        var diff = isScrolledIntoView(elem, elem.parent());
                        if (diff != 0) {
                            diff = diff + 5*(diff/Math.abs(diff));
                            elem.parent().scrollToPos(elem.parent().scrollTop() - diff, 0);
                            arrowNavigationTimestamp = new Date().getTime();
                        }
                    }
                }

                function firstInList() {
                    return angular.element(element.find('li:not(.ng-hide)').first());
                }

                function fokusItem() {
                    return angular.element(element.find('li.harFokus:not(.ng-hide)'));
                }

                function hentValgtTekstBasertPaaValue() {
                    var idx = scope.options.indexByValue(scope.ngModel);

                    if (idx > -1) {
                        return scope.options[idx].text;
                    }
                    return '';
                }

                function isScrolledIntoView(element, container) {
                    var containerTop = container.offset().top;
                    var containerBottom = containerTop + container.height();

                    var elemTop = element.offset().top;
                    var elemBottom = elemTop + element.height();

                    var diffTop = containerTop - elemTop; // Hvis > 0, må scrolle ned
                    var diffBottom = containerBottom - elemBottom; // Hvis < 0 må scrolle opp

                    if (diffTop > 0) {
                        return diffTop;
                    } else if (diffBottom < 0) {
                        return diffBottom;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }]);