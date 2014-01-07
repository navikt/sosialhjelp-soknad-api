angular.module('nav.booleanradio', ['nav.cmstekster', 'nav.input'])
    .directive('booleanradio', [function () {
        return {
            restrict: "A",
            replace: true,
            transclude: true,
            scope: true,
            require: ['navFaktum', '^form'],
            link: {
                pre: function (scope, element, attrs) {
                    var src = attrs.nokkel;
                    scope.navModel = attrs.navModel;
                    scope.sporsmal = src + ".sporsmal";
                    scope.trueLabel = src + ".true";
                    scope.falseLabel = src + ".false";
                    scope.navfeilmelding = src + ".feilmelding";
                },
                post: function (scope, element) {
                    scope.hvisModelErTrue = function () {
                        return scope.faktum.value == 'true';
                    };

                    scope.hvisModelErFalse = function () {
                        return scope.faktum.value && !scope.hvisModelErTrue();
                    };

                    scope.skalViseTranscludedInnhold = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return scope.hvisModelErFalse() && transcludeElement.text().length > 0;
                    };

                    scope.hvisIkkeIEkstraSpmBoks = function() {
                        return !element.parents().hasClass('ekstra-spm-boks');
                    };
                }
            },
            templateUrl: '../js/common/directives/booleanradio/booleanradioTemplate.html'
        }
    }]);
