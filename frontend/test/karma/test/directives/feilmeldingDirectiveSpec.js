describe('feilmeldinger', function () {
    var scope, element, timeout;

    var requiredFeil = 'Er required';

    beforeEach(module('nav.feilmeldinger', 'templates-main', 'nav.cmstekster'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'error.key': requiredFeil
        }});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        timeout = $timeout;

        element = angular.element('<div data-ng-form="form"><div form-errors></div><div class="form-linje"><input type="text" ng-model="inputModel" ng-required="true" error-messages="\'error.key\'"></div></div>');
        scope = $rootScope;

        $compile(element)(scope);
        scope.$apply();
    }));

    it('feilmelding skal ikke vises før validering kjøres', function() {
        expect(element.find('li').length).toBe(0);
    });

    it('feilmelding skal vises dersom ett inputfelt har error-messages satt og feltet inneholder feil', function() {
        scope.runValidation(false);
        scope.$apply();
        expect(element.find('li').length).toBe(1);
    });

    it('feilmelding skal ha rett tekst', function() {
        scope.runValidation(false);
        scope.$apply();
        expect(element.find('li').text().trim()).toBe(requiredFeil);
    });

    it('skal scrolle til feilmeldingslisten dersom runvalidation blir kalt med true', function() {
        timeout(function() {
            spyOn(window, 'scrollToElement');
            scope.runValidation(true);
            expect(window.scrollToElement).toHaveBeenCalled();
        }, 1);
    });

    it('skal scrolle til feilmelding dersom man klikker på den', function() {
        scope.runValidation(false);
        scope.$apply();
        spyOn(window, 'scrollToElement');
        element.find('li').triggerHandler('click');
        expect(window.scrollToElement).toHaveBeenCalled();
    });

    it('skal fjerne feilmelding når feilen blir rettet', function() {
        var input = element.find('input');
        scope.runValidation(false);
        scope.$apply();

        input.val("input");
        input.trigger('input');

        expect(element.find('li').length).toBe(0);
    });
});
