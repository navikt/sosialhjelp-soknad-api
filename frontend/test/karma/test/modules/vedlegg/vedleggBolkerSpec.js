describe('vedleggbolker', function () {
    var element, scope, timeout, form, inputmodell;

    beforeEach(module('nav.vedlegg', 'nav.cms', 'templates-main', 'nav.accordion'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="formname" data-trigg-bolker>' +
                '<div data-accordion>' +
                '<div data-accordion-group>' +
                '<div class="vedlegg-bolk"></div>" ' +
                '<div class="bolk1"></div>" ' +
                '</div>' +
                '<div data-accordion-group>' +
                '<div class="vedlegg-bolk"></div>" ' +
                    '<input type="text" required data-ng-model="modell" name="modellname"> ' +
                '</div> ' +
                '</div> ' +
                '<a href="javascript:void(0)" id="til-oppsummering"></a>' +
                '</form>');


        $compile(element)(scope);
        element.appendTo(document.body);
        scope.$apply();
        form = scope.formname;
        inputmodell = form.modellname;
    }));

    beforeEach(function () {
        jasmine.Clock.useMock();
    });
    afterEach(function () {
        element.remove();
    });

    it('Forste bolk som inneholder feil, dvs har klassen ekstraVedlegg eller ikke har behandlet, skal få klassen open', function () {
        timeout.flush();
        scope.$apply();
        var bolkMedFeil = element.find('.accordion-group').first();
        var bolkMedFeilSist = element.find('.accordion-group').last();
        expect(bolkMedFeil.hasClass('open')).toBe(true);
        expect(bolkMedFeilSist.hasClass('open')).toBe(false);
    });
    it('Bolk med klassen behandlet skal ikke få klassen open', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        var vedlegg = accordionGroup1.find('.vedlegg-bolk');
        vedlegg.addClass("behandlet");

        timeout.flush();
        scope.$apply();
        var accordionGroup2 = element.find('.accordion-group').last();

        expect(accordionGroup1.hasClass('open')).toBe(false);
        expect(accordionGroup2.hasClass('open')).toBe(true);
    });
    it('Bolk med klassen ekstraVedlegg skal få klassen open', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        var vedlegg = accordionGroup1.find('.vedlegg-bolk');
        vedlegg.addClass("ekstraVedlegg");

        timeout.flush();
        scope.$apply();
        expect(accordionGroup1.hasClass('open')).toBe(true);
    });
    it('Bolk med klassen open og ikke validert skal fortsatt ha klassen open', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        accordionGroup1.addClass('open');
        timeout.flush();
        scope.$apply();
        expect(accordionGroup1.hasClass('open')).toBe(true);
    });
    it('Bolk har klasse open og vedleggsiden valideres skal denne bolken lukkes (får ikke testet klikket)', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        accordionGroup1.addClass('open');
        var vedlegg = accordionGroup1.find('.vedlegg-bolk');
        vedlegg.addClass('behandlet');

        timeout.flush();
        scope.$apply();

        element.find('#til-oppsummering').triggerHandler('click');
    });
    it('Bolk er lukket og validert, skal fortsatt være lukket', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        var vedlegg = accordionGroup1.find('.vedlegg-bolk');
        vedlegg.addClass('behandlet');

        timeout.flush();
        scope.$apply();

        element.find('#til-oppsummering').triggerHandler('click');
        expect(accordionGroup1.hasClass('open')).toBe(false);
    });
    it('hvis formen er valid så skal det ikke skje noe med bolkene', function () {
        var accordionGroup1 = element.find('.accordion-group').first();
        inputmodell.$setViewValue("valid");

        timeout.flush();
        scope.$apply();

        expect(accordionGroup1.hasClass('open')).toBe(true);
        element.find('#til-oppsummering').triggerHandler('click');
        expect(accordionGroup1.hasClass('open')).toBe(true);
    });
});
describe('vedleggbolker', function () {
    var element, scope, timeout, form, inputmodell;

    beforeEach(module('nav.vedlegg', 'nav.cms', 'templates-main', 'nav.accordion'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="formname" data-trigg-bolker>' +
                '<div data-accordion>' +
                    '<div data-accordion-group>' +
                        '<div class="vedlegg-bolk"></div>" ' +
                        '<div class="bolk1"></div>" ' +
                    '</div>' +
                '</div> ' +
                '<a href="javascript:void(0)" data-apne-annet-vedlegg data-ng-click="nyttAnnetVedlegg()"></a>' +
                '</form>');


        $compile(element)(scope);
        element.appendTo(document.body);
        scope.$apply();
        form = scope.formname;
        inputmodell = form.modellname;
    }));

    beforeEach(function () {
        jasmine.Clock.useMock();
    });
    afterEach(function () {
        element.remove();
    });

    it('Forste bolk som inneholder feil, dvs har klassen ekstraVedlegg eller ikke har behandlet, skal få klassen open', function () {
        element.find('a').triggerHandler('click');
        jasmine.Clock.tick(20000);
        timeout.flush();
    });
});