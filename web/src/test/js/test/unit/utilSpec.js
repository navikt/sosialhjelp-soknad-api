describe('utility funksjoner -', function () {

    describe('Array prototype:', function () {

        var array, objArray;

        beforeEach(function () {
            array = [1, 2, 3];
            objArray = [
                {a: 1, 2: 'b'}
            ];
        });

        it('skal returnere siste element i array', function () {
            expect(array.last()).toEqual(3);
        });

        it('skal returnere true når array inneholder spesifisert element', function () {
            expect(array.contains(2)).toEqual(true);
        });

        it('skal returnere false når array ikke inneholder spesifisert element', function () {
            expect(array.contains(4)).toEqual(false);
        });

        it('skal returnere true dersom ett objekt i ett array har verdien 1', function () {
            expect(objArray.containsObjectWithValue(1)).toBe(true);
        });

        it('skal returnere true dersom ett objekt i ett array har verdien b', function () {
            expect(objArray.containsObjectWithValue('b')).toBe(true);
        });

        it('skal returnere false dersom ingen objekter i ett array har gitt verdi', function () {
            expect(objArray.containsObjectWithValue(2)).toBe(false);
        });
    });

    describe('Sjekk om element er true', function () {
        it('skal returnere false dersom elementet er undefined', function () {
            var elm;
            expect(checkTrue(elm)).toEqual(false);
        });

        it('skal returnere true dersom elementet er en string som sier true', function () {
            var elm = "true";
            expect(checkTrue(elm)).toEqual(true);
        });

        it('skal returnere false dersom elementet er en string som sier false', function () {
            var elm = "false";
            expect(checkTrue(elm)).toEqual(false);
        });

        it('skal returnere false dersom elementet er en string som ikke sier true', function () {
            var elm = "abc123";
            expect(checkTrue(elm)).toEqual(false);
        });
    });

    describe('Sjekk deep clone av et objekt', function () {
        var obj;

        beforeEach(function () {
            obj = {1: 'a'};
        });

        it('deep clone skal klone ett objekt', function () {
            var objDeepClone = deepClone(obj);
            expect(Object.keys(objDeepClone)).toEqual(Object.keys(obj));
        });

        it('endringer i ett objekt skal ikke propageres til ett klonet objekt', function () {
            var ikkeDeepClone = obj;
            var objDeepClone = deepClone(obj);
            obj[2] = 'b';

            expect(Object.keys(obj).length).toEqual(2);
            expect(Object.keys(ikkeDeepClone)).toEqual(Object.keys(obj));
            expect(Object.keys(objDeepClone)).not.toEqual(Object.keys(obj));
        });
    });

    describe('Sjekk attributt av java-objekt', function () {
        it('skal returnere true for objekt som inneholder attributt uten angular-prefix', function () {
            var obj = {attributt: 1};

            expect(harAttributt(obj, 'attributt')).toBe(1);
        });

        it('skal returnere false for objekt som ikke inneholder attributt uten angular-prefix', function () {
            var obj = {attributt: 1};

            expect(harAttributt(obj, 'blah')).toBe(false);
        });

        it('skal returnere true for objekt som inneholder attributt med angular-prefix men uten data-prefix', function () {
            var obj = {'ngAttributt': 1};

            expect(harAttributt(obj, 'attributt')).toBe(1);
        });
    });

    describe('capitalize string', function () {
        it('skal gjøre første bokstaven i en string stor', function () {
            var str = "tekst";
            var expectedStr = "Tekst";
            expect(capitalizeFirstLetter(str)).toBe(expectedStr);
        });

        it('skal ikke endre tekst med stor forbokstav', function () {
            var str = "Tekst";
            var expectedStr = "Tekst";
            expect(capitalizeFirstLetter(str)).toBe(expectedStr);
        });
    });

    describe('opprettEgendefinertFeilmelding', function () {
        it('Skal opprette egendefinert feilmeldingobjekt på errors', function () {
            var feilmelding = opprettEgendefinertFeilmelding();
            expect(feilmelding).toBeDefined();
        });

        it('Egendefinert feilmelding skal inneholde et navn', function () {
            var feilmelding = opprettEgendefinertFeilmelding("navn");
            expect(feilmelding.$name).toBeDefined();
        });

        it('Skal kunne oppgi navn til egendefinert feilmelding', function () {
            var navn = "Navn";
            var feilmelding = opprettEgendefinertFeilmelding(navn);
            expect(feilmelding.$name).toBe(navn);
        });

        it('Skal kunne oppgi error-message for egendefiner feilmelding', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage);
            expect(feilmelding.$errorMessages).toBe(errormessage);
        });

        it('Skal kunne oppgi valid for egendefiner feilmelding', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, valid);
            expect(feilmelding.$valid).toBe(valid);
        });

        it('Invalid skal bli satt automatisk til det motsatte av valid', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, valid);
            expect(feilmelding.$invalid).toBe(!valid);
        });

        it('Skal kunne sette om en feilmelding skal være den eneste som vises', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var skalVisesAlene = true;

            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, valid, skalVisesAlene);
            expect(feilmelding.$skalVisesAlene).toBe(skalVisesAlene);
        });
    });

    describe('settEgendefinertFeilmeldingsverdi', function () {
        var scope, form, element;

        beforeEach(inject(function ($compile, $rootScope) {
            scope = $rootScope;
            element = angular.element(
                '<form name="form"></form>'
            );
            scope.permiteringProsent = '';
            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            element.scope().$apply();
        }));

        it('Skal returnere true når feilmeldingsvaliditeten endres fra true til false', function () {
            var valid = true;
            form.$error['feilmeldingskategori'] = [];

            form.$error['feilmeldingskategori'].push(opprettEgendefinertFeilmelding('navn', 'errormessage', valid));
            settEgendefinertFeilmeldingsverdi(form, 'feilmeldingskategori', 'navn', false);

            expect(form.$error['feilmeldingskategori'][0].$valid).toEqual(false);
        });

    });
    describe('leggTilFeilmeldingHvisDenIkkeFinnes', function () {
        var scope, form, element;

        beforeEach(inject(function ($compile, $rootScope) {
            scope = $rootScope;
            element = angular.element(
                '<form name="form"></form>'
            );
            scope.permiteringProsent = '';
            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            element.scope().$apply();
        }));

        it('Skal kunne legge til feilmelding hvis den ikke finnes ', function () {
            var valid = true;
            form.$error['feilmeldingskategori'] = [];

            expect(form.$error['feilmeldingskategori'].length).toEqual(0);
            leggTilFeilmeldingHvisDenIkkeFinnes('feilmeldingskategori', 'feilmeldingsnavn', form, 'feilmelding', true, false)
            expect(form.$error['feilmeldingskategori'].length).toEqual(1);
        });

    });

});


