angular.module('nav.datepicker.service', [])
    .factory('datepickerInputService', function(datepickerInputKeys, datepickerNonInputKeys) {
        var allowedKeys = datepickerInputKeys.concat(datepickerNonInputKeys);

        return {
            isValidInput: function(keyCode, currentLength, maxLength) {
                function isAllowedPeriod() {
                    return (keyCode === 190 && (currentLength === 2 || currentLength === 5) || keyCode !== 190);
                }

                return allowedKeys.contains(keyCode) && (datepickerNonInputKeys.contains(keyCode) || currentLength < maxLength) && isAllowedPeriod();
            }
        };
    });