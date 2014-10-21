angular.module('nav.booleanradioUtenfaktum', ['nav.cmstekster', 'nav.input'])
	.directive('booleanradioUtenfaktum', ['cms', function (cms) {
		return {
			restrict   : 'A',
			replace    : true,
			transclude : true,
			scope      : true,
			require    : ['^form'],
			link       : {
				pre : function (scope, element, attrs) {
					var src = attrs.nokkel;
					scope.navModel = attrs.navModel;
					scope.sporsmal = src + '.sporsmal';
					scope.trueLabel = src + '.true';
					scope.falseLabel = src + '.false';
					scope.navfeilmelding = src + '.feilmelding';
                    scope.radiomodel = null;
                    scope.hjelpetekst = {
                        tittel: src + '.hjelpetekst.tittel',
                        tekst: src + '.hjelpetekst.tekst'
                    };
				},
				post: function (scope, element) {
					scope.hvisModelErTrue = function () {
						return scope.radiomodel === 'true';
					};

					scope.hvisModelErFalse = function () {
						return scope.radiomodel && !scope.hvisModelErTrue();
					};

					scope.vis = function () {
						return scope.hvisModelErFalse();
					};

                    scope.skalViseTranscludedInnhold = function () {
                        return element.find('.ng-transclude').text().trim().length > 0;
                    };

					scope.hvisIkkeIEkstraSpmBoks = function () {
						return !element.parents().hasClass('ekstra-spm-boks');
					};

                    scope.hvisHarHjelpetekst = function() {
                        return cms.tekster[scope.hjelpetekst.tittel] !== undefined;
                    };
				}
			},
			templateUrl: '../js/common/directives/booleanradio/booleanradioUtenfaktumTemplate.html'
		};
	}]);
