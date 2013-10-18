angular.module('nav.ytelser',[])
    .directive('ikkeYtelserValidering', function(){
        return {
            require: 'ngModel',
            scope: {
                model: '=ngModel'
            },
            link: function(scope, elm, attrs, ctrl) {
                var checkboxes = elm.closest('form').find('#harYtelser').find('input');

                checkboxes.change(function () {
                    if ($(this).is(':checked') && checkTrue(scope.model)) {
                        scope.model = false;
                    }

                    if (!checkboxes.is(':checked')){
                        ctrl.$setValidity('harValgtYtelse', true);
                    }
                });

                scope.$watch('model', function(value) {
                    var erAndreCheckboxerAvhuket = checkboxes.is(':checked');
                    if (checkTrue(value) && erAndreCheckboxerAvhuket) {
                        scope.model = false;
                        ctrl.$setValidity('harValgtYtelse', false);
                    }
                });
            }
        };

    });