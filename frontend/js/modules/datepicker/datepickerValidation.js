angular.module('nav.datepicker.validation', [])
    .directive('dateRequired', function() {
        return {
            require: 'ngModel',
            scope: {
                required: '=?dateRequired'
            },
            link: function(scope, element, attrs, ngModel) {
                if (scope.required) {
                    ngModel.$validators.testValidator = function(modelValue, viewValue) {
                        console.log(123, value, v);ø
                        return true;
                    }
                }
            }
        };
    });