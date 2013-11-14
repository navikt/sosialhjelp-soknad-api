angular.module('nav.select', ['ngSanitize'])
    .directive('navSelect', ['$document', '$filter', 'data', function ($document, $filter, data) {
        return {
            scope: {
                options: '=',
                defaultValue: '@',
                filters: '@'
            },
            replace: true,
            templateUrl: '../js/app/directives/select/selectTemplate.html',
            link: function (scope, element) {
                scope.selectOpen = false;
                scope.selected = (scope.defaultValue) ? scope.defaultValue : '';
                scope.inputValue = hentValgtTekstBasertPaaValue();
                scope.searchFilter = '';

                var filters = scope.filters.split(',');

                for(var i = 0; i < filters.length; i++) {
                    var filter = filters[i].split(':')[0];
                    var arg;

                    try {
                        arg = scope.$parent.$eval(filters[i].split(':')[1]);
                    } catch(e) {
                        arg = filters[i].split(':')[1];
                    }

                    scope.options = $filter(filter)(scope.options, arg);
                }

                var arrowNavigationTimestamp = new Date().getTime();
                var input = angular.element(element.find('input'));
                var standardFeilmeldingKey = 'select.feilmelding';

                element.bind('click', function(event) {
                    apne();
                    scope.$apply();
                    event.stopPropagation();
                });

                $document.bind('click', function() {
                    avbryt();
                    scope.$apply();
                });

                scope.selectionChanged = function(event) {
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
                    scope.searchFilter = scope.inputValue;
                });

                scope.keypress = function() {
                    apne();
                }

                scope.skalViseListen = function() {
                    var listeLengde = element.find('li').length;

                    if (listeLengde == 0) {
                        element.addClass('feil');
                        element.find('.melding').text(data.tekster[standardFeilmeldingKey]);
                    } else {
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
                    var listeLengde = element.find('li').length;
                    if (listeLengde == 0) {
                        scope.inputValue = '';
                    } else {
                        scope.selected = item.attr('data-value');
                        scope.inputValue = hentValgtTekstBasertPaaValue();
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
                    }
                }

                function avbryt() {
                    if (scope.selectOpen) {
                        scope.inputValue = '';
                    }
                    lukk();
                }

                function lukk() {
                    scope.selectOpen = false;
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
                    return angular.element(element.find('li').first());
                }

                function fokusItem() {
                    return angular.element(element.find('li.harFokus'));
                }

                function hentValgtTekstBasertPaaValue() {
                    var idx = scope.options.indexByValue(scope.selected);

                    if (idx > -1) {
                        return scope.options[idx].text;
                    }
                    return '';
                }

                function selectedItem() {
                    return angular.element(element.find('li[data-value=' + scope.selected + ']'));
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
    }])
    .filter('filterSearch', [function () {
        return function (input, query) {
            query = query.trim();
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
    }]);