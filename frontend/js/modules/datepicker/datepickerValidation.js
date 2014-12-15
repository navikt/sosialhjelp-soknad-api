angular.module('nav.datepicker.validation', [])
    .directive('dateFormat', function(dateService) {
        return {
            require: 'ngModel',
            scope: false,
            link: function(scope, element, attrs, ngModel) {
                ngModel.$validators.dateFormat = function(modelValue, viewValue) {
                    return dateService.hasCorrectDateFormat(viewValue) || !(scope.disabled === undefined || scope.disabled === false);
                };
            }
        };
    })
    .directive('validDate', function(dateService) {
        return {
            require: 'ngModel',
            scope: false,
            link: function(scope, element, attrs, ngModel) {
                ngModel.$validators.validDate = function(modelValue) {
                    return dateService.isValidDate(modelValue) || !(scope.disabled === undefined || scope.disabled === false);
                };
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
                        return !dateService.isFutureDate(viewValue) || !(scope.disabled === undefined || scope.disabled === false);
                    };
                }
            }
        };
    });