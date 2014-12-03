angular.module('nav.datepicker.validation', [])
    .directive('dateFormat', function(dateService) {
        return {
            require: 'ngModel',
            scope: false,
            link: function(scope, element, attrs, ngModel) {
                ngModel.$validators.dateFormat = function(modelValue, viewValue) {
                    return dateService.hasCorrectDateFormat(viewValue);
                }
            }
        };
    })
    .directive('validDate', function(dateService) {
        return {
            require: 'ngModel',
            scope: false,
            link: function(scope, element, attrs, ngModel) {
                ngModel.$validators.validDate = function(modelValue, viewValue) {
                    return dateService.isValidDate(viewValue);
                }
            }
        };
    })
    .directive('futureDate', function(dateService, $parse) {
        return {
            require: 'ngModel',
            scope: false,
            link: function(scope, element, attrs, ngModel) {
                if (!$parse(attrs.futureDate)(scope)) {
                    ngModel.$validators.futureDate = function(modelValue, viewValue) {
                        return !dateService.isFutureDate(viewValue);
                    }
                }
            }
        };
    });