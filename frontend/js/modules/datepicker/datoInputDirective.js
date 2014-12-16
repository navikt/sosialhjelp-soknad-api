angular.module('nav.datepicker.datoinput', ['ui.bootstrap.datepicker'])
    .directive('datoInput', function ($timeout) {
    return {
        restrict   : 'A',
        require    : '^form',
        replace    : true,
        templateUrl: '../js/modules/datepicker/templates/customDateInputTemplate.html',
        scope      : {
            model: '=datoInput',
            harFokus: '=',
            datepickerClosed:'=',
            erRequired: '=?',
            erFremtidigdatoTillatt: '=?',
            lagre: '&',
            requiredErrorMessage: '@',
            name: '@',
            disabled: '=?'
        },
        link: function (scope, element, attrs, form) {
            var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
            scope.$on(eventForAValidereHeleFormen, function () {
                form[scope.name].$touched = true;
                form[scope.name].$untouched = false;
            });

            scope.blur = function () {
                scope.harFokus = false;

                if (scope.lagre) {
                    $timeout(scope.lagre, 100);
                }
            };

            scope.focus = function () {
                scope.harFokus = true;
            };
        }
    };
})
    .directive('restrictInput', function ($filter, cmsService, datepickerInputService, dateService) {
        return {
            restrict: 'A',
            require : 'ngModel',
            link    : function (scope, element, attrs, ngModel) {
                var datoMask = cmsService.getTrustedHtml('dato.format');
                var caretPosisjonElement = element.closest('.datepicker').find('.caretPosition');
                caretPosisjonElement.hide();

                element.bind('blur', function () {
                    caretPosisjonElement.hide();
                });

                element.bind('focus', function () {
                    caretPosisjonElement.show();
                });

                // Dersom det er ønskelig å sjekke tekst som blir limt inn, kan vi binde til paste
                element.bind('keydown', function (event) {
                    var hasSelectedText = element[0].selectionEnd - element[0].selectionStart > 0;
                    return datepickerInputService.isValidInput(event, element.val().length, datoMask.length, hentCaretPosisjon(element), hasSelectedText);
                });

                ngModel.$formatters.unshift(function (dato) {
                    if (dato) {
                        var datoSomDateObjekt = new Date(dato);
                        return $filter('date')(datoSomDateObjekt, 'dd.MM.yyyy');
                    } else {
                        return '';
                    }
                });

                var oldInput = '';
                ngModel.$parsers.unshift(function (input) {
                    var slettet = input.length < oldInput.length;
                    var caretPosition = hentCaretPosisjon(element);

                    if (!slettet) {
                        var res = datepickerInputService.addPeriodAtRightIndex(input, oldInput, caretPosition);
                        input = res[0];
                        caretPosition = res[1];
                    }
                    oldInput = input;
                    element.val(input);
                    settCaretPosisjon(element, caretPosition);
                    return dateService.reverseNorwegianDateFormat(input);
                });
            }
        };
    })
    .directive('datoMask', function (cssService, maskService) {
        return {
            restrict: 'A',
            link: {
                pre: function(scope, element) {
                    element.after('<span class="caretPosition"></span>');
                },
                post: function(scope, element) {
                    var inputElement = element.prev();
                    var caretPositionElement = element.next();
                    var paddingTop = cssService.getComputedStyle(inputElement, 'padding-top');
                    var inputElementLeft = inputElement.position().left + cssService.getComputedStyle(inputElement, 'padding-left');

                    scope.$watch(function() {
                        return inputElement.val();
                    }, function(newValue) {
                        var text = newValue;
                        if (text === undefined) {
                            text = '';
                        }

                        caretPositionElement.text(text);
                        var left = inputElementLeft + caretPositionElement.outerWidth();
                        var top = inputElement.position().top + paddingTop - 3;
                        element.css({top: top + 'px', left: left + 'px'});

                        element.text(maskService.getMaskText(text));
                    });
                }
            }
        };
    });
