angular.module('nav.sidetittel', [])
	.directive('sidetittel', function ($document, $filter) {
		return function (scope, element, attrs) {
			$document[0].title = $filter('cmstekst')(attrs.sidetittel);
		};
	});
