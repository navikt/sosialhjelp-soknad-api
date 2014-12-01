angular.module('nav.datepicker.service', [])
    .factory('datepickerInputService', function(datepickerInputKeys, datepickerNonInputKeys) {
        var allowedKeys = datepickerInputKeys.concat(datepickerNonInputKeys);

        return {
            isValidInput: function(keyCode, currentLength, maxLength, caretPosition) {
                function isAllowedPeriod() {
                    return (keyCode === 190 && (caretPosition === 2 || caretPosition === 5) || keyCode !== 190);
                }

                return allowedKeys.contains(keyCode) && (datepickerNonInputKeys.contains(keyCode) || currentLength < maxLength) && isAllowedPeriod();
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
    });