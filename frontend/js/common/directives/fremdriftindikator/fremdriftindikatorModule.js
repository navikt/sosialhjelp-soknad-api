angular.module('nav.fremdriftsindikator', [])
	.directive('fremdriftsindikator', ['$compile', function ($compile) {
		return {
			restrict: 'A',
			link    : function (scope, element, attrs) {
                var farge = attrs.fremdriftsindikator;
                var img;
                if (farge === 'grå') {
                    img = '<img src="../img/ajaxloader/graa/loader_graa_48.gif"/>';
                } else if (farge === 'rød') {
                    img = '<img src="../img/ajaxloader/roed/loader_rod_48.gif"/>';
                } else if (farge === 'svart') {
                    img = '<img src="../img/ajaxloader/svart/loader_svart_48.gif"/>';
                } else {
                    img = '<img src="../img/ajaxloader/hvit/loader_hvit_48.gif"/>';
                }

				var spinner = angular.element(img);
				spinner.insertAfter(element);
				$compile(spinner)(scope);

				scope.$watch('fremdriftsindikator.laster', function (value) {
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
