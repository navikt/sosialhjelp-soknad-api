angular.module('nav.datepicker.service', [])
    .factory('datepickerInputService', function(datepickerInputKeys, datepickerUtilityKeys, periodKeyCode, ctrlKeyCodes) {
        return {
            isValidInput: function(event, currentLength, maxLength, caretPosition, hasSelectedText) {
                function isAllowedPeriod() {
                    return (keyCode === periodKeyCode && (caretPosition === 2 || caretPosition === 5) || keyCode !== periodKeyCode);
                }

                function isRegularInputAllowed() {
                    return hasSelectedText || currentLength < maxLength;
                }

                function isAllowedUtilityInput() {
                    return datepickerUtilityKeys.contains(keyCode) || ctrlPressed && ctrlKeyCodes;
                }

                var keyCode = event.keyCode, ctrlPressed = event.ctrlKey;

                return isAllowedUtilityInput() || (datepickerInputKeys.contains(keyCode) && isAllowedPeriod() && isRegularInputAllowed());
            },
            addPeriodAtRightIndex: function(input, oldInput, caretPosition) {
                var start = caretPosition - (input.length - oldInput.length);
                var slutt = caretPosition;
                for (var i = start; i < slutt && i < input.length; i++) {
                    if (i === 1 || i === 4) {
                        if (input[i + 1] === '.') {
                            caretPosition++;
                            i++;
                        } else {
                            input = input.splice(i + 1, 0, '.');
                            caretPosition++;
                            i++;
                            slutt++;
                        }
                    }
                }
                return [input, caretPosition];
            }
        };
    })
    .factory('maskService', function(cmsService) {
        return {
            getMaskText: function(text) {
                var dayRegExp = new RegExp(/^(\d){0,2}$/);
                var monthRegExp = new RegExp(/^\d\d\.(\d){0,2}$/);
                var yearRegExp = new RegExp(/^\d\d\.\d\d\.(\d){0,4}$/);
                var initialMaskText = cmsService.getTrustedHtml('dato.format');

                if (dayRegExp.test(text) || monthRegExp.test(text) || yearRegExp.test(text)) {
                    return initialMaskText.substring(text.length, initialMaskText.length);
                } else {
                    return '';
                }
            }
        };
    })
    .factory('deviceService', function() {
        return {
            isTouchDevice: function() {
                return erTouchDevice() && getIEVersion() < 0;
            }
        };
    })
    .factory('cssService', function() {
        return {
            getComputedStyle: function(element, property) {
                var value = window.getComputedStyle(element[0], null).getPropertyValue(property);
                if (value.indexOf('px') !== -1) {
                    value = parseInt(value.substring(0, value.length - 2));
                }

                return value;
            }
        };
    })
    .factory('guidService', function() {
        return {
            getGuid: function() {
                return 'xxxxxxxx'.replace(/[x]/g, function(c) {
                    var r = Math.random()*16| 0, v = c == 'x' ? r : (r&0x3|0x8);
                    return v.toString(16);
                });
            }
        };
    })
    .factory('dateService', function() {
        var dateFormatRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);
        return {
            isValidDate: function(date) {
                if (date) {
                    // stackoverflow.com/questions/5812220/test-if-date-is-valid
                    var bits = date.split('-');
                    var aar = bits[0];
                    var maaned = bits[1];
                    var dag = bits[2];
                    var dagerIMaaned = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

                    // Skuddår
                    if (((aar % 4) === 0 && aar % 100) || (aar % 400) === 0) {
                        dagerIMaaned[1] = 29;
                    }
                    return dag <= dagerIMaaned[--maaned];
                }
                return true;
            },
            reverseNorwegianDateFormat: function(dateString) {
                if (this.hasCorrectDateFormat(dateString) && this.isValidDate(dateString)) {
                    return dateString.split('.').reverse().join('-');
                } else {
                    return '';
                }
            },
            hasCorrectDateFormat: function(date) {
                return dateFormatRegExp.test(date) || date.length === 0;
            },
            isFutureDate: function (dateString) {
                var dateArray = dateString.split(".");

                var date = new Date();
                date.setDate(dateArray[0]);
                date.setMonth(dateArray[1] - 1);
                date.setFullYear(dateArray[2]);
                date.setHours(0);
                date.setMilliseconds(0);
                date.setSeconds(0);
                date.setMinutes(0);

                var today = new Date();
                var temp = new Date();
                temp.setMonth(today.getMonth());
                temp.setDate(today.getDate());
                temp.setFullYear(today.getFullYear());
                temp.setHours(0);
                temp.setMilliseconds(0);
                temp.setSeconds(0);
                temp.setMinutes(0);

                var morgenDagensDatoMillis = temp.setTime(temp.getTime() + 86400000);

                return date.getTime() >= morgenDagensDatoMillis;
            }
        };
    });