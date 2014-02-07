angular.module('nav.dagpengerdirective', [])
	.directive('apneBolker', ['$timeout', function ($timeout) {
		return {
			require: '^form',
			link   : function (scope) {
                $timeout(function() {
                    var forsteInvalidBolk = $('.accordion-group').not('.validert').first();

                    scope.apneTab(forsteInvalidBolk.attr('id'));
                    $timeout(function() {
                        var fokusElement = forsteInvalidBolk.find('input').first();
                        scrollToElement(fokusElement, 400);
                    });
                });
			}
		}
	}]);

angular.module('nav.norskDatoFilter', []).filter('norskdato', function () {
	return function (input) {
		var monthNames = ['Januar', 'Februar', 'Mars', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Desember'];
		if (input) {
			var dag = input.substring(0, 2);
			var mnd = input.substring(3, 5);
			var year = input.substring(6, 10);
			return dag + '. ' + monthNames[mnd - 1] + ' ' + year;
		}
		return input;
	}
});
