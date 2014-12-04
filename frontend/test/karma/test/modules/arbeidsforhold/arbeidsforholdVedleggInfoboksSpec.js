describe('nav.arbeidsforhold.vedlegginfoboks', function() {
    var scope, element;
    beforeEach(module('nav.arbeidsforhold.vedlegginfoboks', 'nav.cms', 'templates-main'));

    describe('nav.arbeidsforhold.vedlegginfoboks', function () {
        function getHtmlVedleggboks(infotekster, vedleggtekster) {
            return "<div data-arbeidsforhold-vedlegg-infoboks " +
                "data-infotekster='" + infotekster + "' " +
                "data-vedleggtekster='" + vedleggtekster + "'></div>";
        }

        it("skal parse liste med vedleggtekster", function () {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlVedleggboks("test", '["tekst1", "tekst2", "tekst3"]'));
                $compile(element)(scope);
                scope.$apply();
            });
            expect(scope.vedleggTekster.length).toBe(3);
            expect(scope.vedleggTekster).toContain("tekst3");
        });

        it('skal parse liste med infotekster', function () {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlVedleggboks('["tekst1", "tekst2", "tekst3"]', ""));
                $compile(element)(scope);
                scope.$apply();
            });
            expect(scope.infoTekster.length).toBe(3);
            expect(scope.infoTekster).toContain("tekst3");
        });
    });
});
