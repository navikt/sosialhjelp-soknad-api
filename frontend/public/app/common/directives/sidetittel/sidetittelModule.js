angular.module('nav.sidetittel', [])
		.directive('sidetittel', ['$document', 'cms', function($document, cms) {
			return function(scope, element, attrs) {
				$document[0].title = cms.tekster[attrs.sidetittel];
			};
		}]);
