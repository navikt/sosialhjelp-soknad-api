angular.module('nav.fremdriftsindikator',[])
    .directive('fremdriftsindikator', ['$compile', function($compile) {
        return {
            restrict: 'A',
            link: function(scope, element) {
                var spinner = angular.element('<img src="../img/ajaxloader/hvit/loader_hvit_48.gif"/>');
                spinner.insertAfter(element);
                $compile(spinner)(scope);

                scope.$watch('fremdriftsindikator.laster', function(value) {
                    if (value) {
                        element.hide();
                        spinner.show();
                    } else {
                        element.show();
                        spinner.hide();
                    }
                });
            }
        };
    }]);