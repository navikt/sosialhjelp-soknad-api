// https://github.com/itsdrewmiller/angular-perferct-scrollbar
angular.module('nav.scrollbar', []).directive('navScroll', ['$parse', '$timeout', function ($parse, $timeout) {
	return function (scope, elem, attr) {
		elem.perfectScrollbar({
			minScrollbarLength: $parse(attr.minScrollbarLength)() || 10,
            suppressScrollX   : $parse(attr.suppressScrollX)() || true
		});

		$timeout(function () {
			elem.perfectScrollbar('update');
		});

        $(window).bind('resize', function() {
            elem.perfectScrollbar('update');
        });
	}
}]);
