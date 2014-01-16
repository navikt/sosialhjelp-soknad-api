// https://github.com/itsdrewmiller/angular-perferct-scrollbar
angular.module('nav.scrollbar', []).directive('navScroll', ['$parse', '$timeout', function ($parse, $timeout) {
	return function (scope, elem, attr) {
		elem.perfectScrollbar({
			wheelSpeed        : $parse(attr.wheelSpeed)() || 50,
			wheelPropagation  : $parse(attr.wheelPropagation)() || false,
			minScrollbarLength: $parse(attr.minScrollbarLength)() || false,
			suppressScrollY   : true
		});

		$timeout(function () {
			elem.perfectScrollbar('update');
		});

        $(window).bind('resize', function() {
            elem.perfectScrollbar('update');
        });
	}
}]);
