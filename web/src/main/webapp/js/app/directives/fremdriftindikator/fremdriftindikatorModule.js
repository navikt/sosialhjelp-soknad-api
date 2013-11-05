angular.module('nav.fremdriftsindikator',[])
    .directive('fremdriftsindikator', ['$compile', function($compile) {
        return {
            restrict: 'A',
            link: function(scope, element) {
                element.attr("data-ng-class", "{true: 'hide', false: 'show'}[laster]");
                var spinner = angular.element('<img ng-class="{true: \'show\', false: \'hide\'}[laster]" src="../img/ajaxloader/hvit/loader_hvit_48.gif"/>');
                spinner.insertAfter(element);
                $compile(spinner)(scope);
            }
        };
    }]);