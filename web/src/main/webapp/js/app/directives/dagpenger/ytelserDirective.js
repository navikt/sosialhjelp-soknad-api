angular.module('nav.ytelser.directive', [])
    .directive('ikkeYtelserValidering', ['$timeout', function ($timeout) {
        return {
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                formname: '='
            },
            link: function (scope, elm, attrs) {
                $timeout(function () {

                    var checkboxes = elm.closest('form').find('#harYtelser').find('input[type="checkbox"]');

                    console.log(checkboxes);
                    checkboxes.change(function () {
                        if ($(this).is(':checked') && checkTrue(scope.model)) {
                            scope.model = false;
                        }

                        if (!checkboxes.is(':checked')) {
                            scope.formname.$setValidity('harValgtYtelse', true);
                        }
                    });

                    scope.$watch('model', function (value) {
                        var erAndreCheckboxerAvhuket = checkboxes.is(':checked');
                        console.log(checkTrue(value));
                        console.log(erAndreCheckboxerAvhuket);
                        if (checkTrue(value) && erAndreCheckboxerAvhuket) {
                            scope.model = false;

                            scope.formname.$setValidity('harValgtYtelse', false);
                        }
                    });
                }, 100);
            }
        };

    }]);
