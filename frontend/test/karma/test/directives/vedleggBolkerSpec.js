describe('vedleggbolker', function () {
    var rootScope, element, scope, timeout, elementNavn, form, elementNavnReq;

    beforeEach(module('nav.vedleggbolker', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="form" data-trigg-bolker>' +
                '<div class="accordion-group"></div>' +
                '<div class="accordion-group">' +
                    '<div class="form-linje" data-checkbox-validate> ' +
                        '<input type="checkbox" required data-ng-model="modell"  name="inputnameReq" data-error-messages="{feilmeldingstekst}"> ' +
                        '<span class="melding"></span>' +
                        '<a href="javascript:void(0)" id="til-oppsummering" ></a>' +
                    '</div> ' +
                '</div> ' +
            '</form>');

        $compile(element)(scope);
        scope.$apply();
        form = scope.form;
        elementNavn = form.inputname;
    }));

    beforeEach(function () {
        jasmine.Clock.useMock();
    });

    describe('triggBolker', function () {
        it('Jqueryselectoren $("") fungerer ikke', function () {
            var bolkMedFeil = element.find('.accordion-group').last();
            timeout.flush();
            expect(bolkMedFeil.hasClass('open')).toBe(false);
            element.find('#til-oppsummering').click();
        });
    });
});