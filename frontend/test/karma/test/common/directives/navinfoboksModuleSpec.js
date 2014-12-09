describe('nav.markup.navinfoboks', function() {
    var scope, element;
    beforeEach(module('nav.markup.navinfoboks', 'nav.cms', 'templates-main'));

    describe('navinfoboks', function() {
        function getHtmlForNavinfoboks(infotekster) {
            return "<div data-navinfoboks data-infotekster='" + infotekster + "'></div>";
        }

        it("skal parse liste med infotekster", function() {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlForNavinfoboks('["tekst1", "tekst2", "tekst3"]'));
                $compile(element)(scope);
                scope.$apply();
                scope = element.find(".infoboks-inner").scope();
            });

            expect(scope.infoTekster.length).toBe(3);
            expect(scope.infoTekster).toContain("tekst3");
        });

        it('skal godta kun en enkel infotekst', function() {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlForNavinfoboks('"testTekstSingle"'));
                $compile(element)(scope);
                scope.$apply();
                scope = element.find(".infoboks-inner").scope();
            });

            expect(scope.infoTekster.length).toBe(1);
            expect(scope.infoTekster).toContain("testTekstSingle");
        });
    });

    describe('vedlegginfoboks', function() {
        function getHtmlForVedlegginfoboks(infotekster) {
            return "<div data-vedlegginfoboks data-vedleggtekster='" + infotekster + "'></div>";
        }

        it("skal parse liste med vedleggtekster", function() {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlForVedlegginfoboks('["tekst1", "tekst2", "tekst3"]'));
                $compile(element)(scope);
                scope.$apply();
                scope = element.find(".ikon-vedlegg-strek").scope();
            });

            expect(scope.vedleggTekster.length).toBe(3);
            expect(scope.vedleggTekster).toContain("tekst3");
        });

        it('skal godta kun en enkel vedleggtekst', function() {
            inject(function ($compile, $rootScope) {
                scope = $rootScope;
                element = angular.element(getHtmlForVedlegginfoboks('"testTekstSingle"'));
                $compile(element)(scope);
                scope.$apply();
                scope = element.find(".ikon-vedlegg-strek").scope();
            });

            expect(scope.vedleggTekster.length).toBe(1);
            expect(scope.vedleggTekster).toContain("testTekstSingle");
        });
    });
});