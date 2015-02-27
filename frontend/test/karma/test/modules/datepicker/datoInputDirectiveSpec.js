describe('datepicker directive', function () {
    var ugyldigFremtidigDatoFeil = 'Ugyldig fremtidig dato';
    var tildatoFeil = 'Ugyldig tildato';
    var formatFeil = 'Ugyldig format';
    var ikkeGyldigDato = 'Ugyldig dato';
    var requiredFeil = 'Required';
    var datoFormat = 'dd.mm.yyyy';
    var label = 'Labeltekst';

    var scope, form, element, rootScope;

    beforeEach(module('nav.datepicker', 'templates-main', 'nav.cms', 'ngMessages'));

    beforeEach(module(function ($provide) {

        $provide.value("cms", {'tekster': {
            'dato.ugyldigFremtidig.feilmelding': ugyldigFremtidigDatoFeil,
            'dato.tilDato.feilmelding': tildatoFeil,
            'dato.format.feilmelding': formatFeil,
            'dato.ikkeGyldigDato.feilmelding': ikkeGyldigDato,
            'required.feil': requiredFeil,
            'dato.format': datoFormat,
            'label.tekst': label
        }});
        $provide.value("data", {});

    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.testModel = "";
        scope.harFokus = false;
        scope.erRequired = true;
        scope.datepickerClosed = false;
        scope.erFremtidigdatoTillatt=true;
        scope.disabled = false;
    }));

    function compileDirective(template) {
        if(template) {
            template =  '<form name="formname">' + template + '</form>';
        } else {
            template = '<form name="formname">' +
                '<div data-dato-input="testModel"' +
                ' data-har-fokus="harFokus"'+
                'data-er-required="erRequired"'+
                'data-datepicker-closed="datepickerClosed"'+
                'data-name="navn"'+
                'data-required-error-message="feilmelding"'+
                'data-er-fremtidigdato-tillatt="true"'+
                'data-lagre="lagre"'+
                'data-disabled="disabled"></div>'+
                '</form>';
        }

        inject(function($compile, $rootScope) {
            var form = $compile(template)($rootScope);
            $rootScope.$apply();

            element = form.find('.form-control');
            scope = element.isolateScope();
        });
        scope.$digest();
    }

    describe('initialisering', function() {
        beforeEach(function () {
          compileDirective();
        });

        it('fremtidigtillattdato skal fremtidige tillate datoer være tillat', function() {
            //TODO legg til tester
//            expect(scope.disableDate(new Date() + 10, 'day')).toBe(true);
        });
    });
});