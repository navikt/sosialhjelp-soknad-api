angular.module('nav.ytelser.directive',[])
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

    })
    .directive('validerYtelser', function(){
        return {
            link: function(scope, elm, attrs) {
                console.log("Hallo");
                console.log(scope);
//                scope.test();
            }
        };
    });



/*
.directive('validerYtelser', function(){
    return {
        require: 'ngModel',
        scope: {
            model: '=ngModel',
            validerYtelser: '='
        },
        link: function(scope, elm, attrs, ctrl) {
            var checkboxes = elm.closest('form').find('input');

            scope.$watch('model', function() {
                var erNoenCheckboxerValgt = checkboxes.is(':checked');
                if (erNoenCheckboxerValgt) {
                    scope.validerYtelser.$setValidity('harTattValg', true);
                } else {
                    scope.validerYtelser.$setValidity('harTattValg', false);
                }
            });
        }
    };

});*/
