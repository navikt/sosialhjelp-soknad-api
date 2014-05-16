describe('hjelpetekst', function () {
    var rootScope, element, scope, timeout, form, event, event2;
    event = $.Event("click");
    event2 = $.Event("click");
    event2.timeStamp = 1;

    beforeEach(module(
        'nav.hjelpetekst',
        'nav.cmstekster',
        'ngSanitize',
        'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
            'hjelpetekst.tekst': 'Hjelpetekst tekst' }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        rootScope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="form" data-trigg-bolker>' +
                '<span data-nav-hjelpetekstelement data-tittel="{{ \'hjelpetekst.tittel\' | cmstekst }}"' +
                'data-tekst="{{ \'hjelpetekst.tekst\' | cmstekst }}"></span>' +
                '</form>');

        $compile(element)(rootScope);
        rootScope.$apply();
        $(window).resize();
        scope = element.find('div').first().isolateScope();
        form = scope.form;
    }));

    describe('hjelpetekst', function () {
        it('tittel og tekst skal bli satt til henholdsvis hjelpeteksttittelen og tekst', function () {
            scope.toggleHjelpetekst();
            scope.$apply();
            var tittel = element.find('.liten-strek');
            expect(tittel.text()).toBe("Tittel hjelpetekst");

            var tekst = element.find('.mini');
            expect(tekst.text()).toBe("Hjelpetekst tekst");
        });
        it('visHjelp skal være false', function () {
            expect(scope.visHjelp).toBe(false);
        });
        it('visHjelp skal være true hvis false og toggleHjelpetekst()', function () {
            expect(scope.visHjelp).toBe(false);
            scope.toggleHjelpetekst();
            expect(scope.visHjelp).toBe(true);
        });
        it('visHjelp skal være false hvis true og lukk())', function () {
            expect(scope.visHjelp).toBe(false);
            scope.toggleHjelpetekst();
            expect(scope.visHjelp).toBe(true);
            scope.lukk(event);
            expect(scope.visHjelp).toBe(false);
        });
        it('hvisHjelp skal settes til false hvis det klikkes i dokumentet', function () {
            expect(scope.visHjelp).toBe(false);
            scope.toggleHjelpetekst();
            expect(scope.visHjelp).toBe(true);
            $(document).click();
            timeout.flush();
            expect(scope.visHjelp).toBe(false);
        });
        it('hvisHjelp skal ikke settes til false hvis det klikkes i dokumentet og timestampen til klikket er det samme som lukkEventTimestamp', function () {
            expect(scope.visHjelp).toBe(false);
            scope.toggleHjelpetekst();
            expect(scope.visHjelp).toBe(true);
            scope.stoppKlikk(event2);
            $(document).click(event2);
            timeout.flush();
            expect(scope.visHjelp).toBe(true);
        });
    });
});
