angular.module('nav.ytelser.directive',[])
    .directive('ikkeYtelserValidering', function(){
        return {
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                form: '='
            },
            link: function(scope, elm, attrs, ctrl) {
                var checkboxes = elm.closest('form').find('#harYtelser').find('input');
                console.log(scope.form);
                checkboxes.change(function () {
                    if ($(this).is(':checked') && checkTrue(scope.model)) {
                        scope.model = false;
                    }

                    if (!checkboxes.is(':checked')){
                        scope.form.$setValidity('harValgtYtelse', true);
                    }
                });

                scope.$watch('model', function(value) {
                    var erAndreCheckboxerAvhuket = checkboxes.is(':checked');
                    if (checkTrue(value) && erAndreCheckboxerAvhuket) {
                        scope.model = false;
                        scope.form.$setValidity('harValgtYtelse', false);
                    }
                });
            }
        };

    });
