angular.module('nav.prosent', [])
    .directive('prosent', [function () {
        return {
            replace: true,
            require: 'ngModel',

            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    var INTEGER_REGEX = /^\-?\d*$/;
                    if (INTEGER_REGEX.test(viewValue) && viewValue <= 100 && viewValue >= 0) {
                        ctrl.$setValidity('prosent', true);
                        return viewValue;
                    } else {
                        ctrl.$setValidity('prosent', false);
                        return undefined;
                    }
                });
            }
        };
    }]);