describe('melding', function () {
    var rootScope, element, scope, timeout, form, event, compile;
    event = $.Event("click");

    beforeEach(module('nav.melding', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {
        });
        $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
            'hjelpetekst.tekst': 'Hjelpetekst tekst' }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        compile = $compile;
        scope.$apply();
    }));

    describe('melding', function () {
        it('SkalVises skal returnere false når form-linjen ikke har klassene feil eller feilstyling', function () {
            element = angular.element(
                '<form name="form">' +
                    '<div class="form-linje"></div>' +
                    '<div data-melding></div> ' +
                '</form>');

            compile(element)(scope);
            scope.$apply();

            expect(scope.skalVises()).toBe(false);
        });

        it('SkalVises skal returnere true når form-linjen har en av klassene feil eller feilstyling', function () {
            element = angular.element(
                '<form name="form">' +
                    '<div class="form-linje feil">' +
                    '<div data-melding></div> ' +
                    '</div>' +
                    '</form>');

            compile(element)(scope);
            scope.$apply();

            expect(scope.skalVises()).toBe(true);
        });
        it('SkalVises skal returnere true når form-linjen har en av klassene feil eller feilstyling', function () {
            element = angular.element(
                '<form name="form">' +
                    '<div class="form-linje feilstyling">' +
                        '<div data-melding></div> ' +
                    '</div>' +
                '</form>');

            compile(element)(scope);
            scope.$apply();

            expect(scope.skalVises()).toBe(true);
        });
        it('SkalVises skal returnere true når form-linjen har klassene feil eller feilstyling', function () {
            element = angular.element(
                '<form name="form">' +
                    '<div class="form-linje feilstyling feil">' +
                    '<div data-melding></div> ' +
                    '</div>' +
                    '</form>');

            compile(element)(scope);
            scope.$apply();

            expect(scope.skalVises()).toBe(true);
        });
    });
});