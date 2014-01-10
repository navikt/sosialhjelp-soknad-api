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

        it('skal returnere at indeksen i arrayet til objektet er 0', function () {
            expect(objArray.indexByValue(1)).toBe(0);
        });

        it('skal returnere at indeksen i arrayet til objektet er 0', function () {
            expect(objArray.indexByValue('b')).toBe(0);
        });

        it('skal returnere -1 dersom ingen objekter i arrayet har gitt verdi', function () {
            expect(objArray.indexByValue(2)).toBe(-1);
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

        it('Skal kunne oppgi string for å referere til ett element', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var referanse = "Ref";
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, referanse);
            expect(feilmelding.$linkId).toBe(referanse);
        });

        it('Skal kunne oppgi valid for egendefiner feilmelding', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var referanse = "Ref";
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, referanse, valid);
            expect(feilmelding.$valid).toBe(valid);
        });

        it('Invalid skal bli satt automatisk til det motsatte av valid', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var referanse = "Ref";
            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, referanse, valid);
            expect(feilmelding.$invalid).toBe(!valid);
        });

        it('Skal kunne sette om en feilmelding skal være den eneste som vises', function () {
            var navn = "Navn";
            var errormessage = "Error";
            var valid = true;
            var referanse = "Ref";
            var skalVisesAlene = true;

            var feilmelding = opprettEgendefinertFeilmelding(navn, errormessage, referanse, valid, skalVisesAlene);
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

        it('Skal kunne legge til feilmelding', function () {
            var valid = false;

            settEgendefinertFeilmeldingsverdi(form, 'feilmeldingskategori', 'navn', 'errormessage', 'referanseTilElement', valid, false);
            expect(form.$error['feilmeldingskategori'].length).toEqual(1);
            expect(form.$error['feilmeldingskategori'][0].$valid).toEqual(false);
        });

        it('Feilmelding skal fjernes dersom den endres til valid', function () {
            var valid = false;

            settEgendefinertFeilmeldingsverdi(form, 'feilmeldingskategori', 'navn', 'errormessage', 'referanseTilElement', valid, false);
            expect(form.$error['feilmeldingskategori'].length).toEqual(1);
            expect(form.$error['feilmeldingskategori'][0].$valid).toEqual(false);

            settEgendefinertFeilmeldingsverdi(form, 'feilmeldingskategori', 'navn', 'errormessage', 'referanseTilElement', true, false);
            expect(form.$error['feilmeldingskategori'].length).toEqual(0);
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
            form.$error['feilmeldingskategori'] = [];
            expect(form.$error['feilmeldingskategori'].length).toEqual(0);
            leggTilFeilmeldingHvisDenIkkeFinnes(form, 'feilmeldingskategori', 'feilmeldingsnavn', 'feilmelding', 'feilmeldingElementNavn', true, false)
            expect(form.$error['feilmeldingskategori'].length).toEqual(1);
        });

    });

    describe('reversere norsk datoformat (fra dd.MM.yyyy til yyyy.MM.dd', function () {
        it('skal få returverdi', function () {
            var dato = reverserNorskDatoformat("");
            expect(dato).toBeDefined();
        });

        it('skal få tom string dersom datostringen er på galt format', function () {
            var dato = reverserNorskDatoformat("");
            expect(dato).toBe("");
        });

        it('skal få tilbake string med innhold dersom datostringen er på rett format', function () {
            var dato = reverserNorskDatoformat("01.01.2013");
            expect(dato.length).toBeGreaterThan(0);
        });

        it('skal få tilbake dato på format yyyy-MM-dd', function () {
            var forventetDatoFormattering = '2013-01-01';
            var dato = reverserNorskDatoformat("01.01.2013");
            expect(dato).toBe(forventetDatoFormattering);
        });
    });

    describe('datovalidering', function() {
        it('skal få resultat tilbake fra datovalidering', function() {
            var datoString = "01.01.2010";
            var erGyldig = erGyldigDato(datoString);

            expect(erGyldig).toBeDefined();
        });

        it('skal få tilbake boolean fra datovalidering', function () {
            var datoString = "01.01.2010";
            var erGyldig = erGyldigDato(datoString);

            expect(typeof erGyldig).toBe("boolean");
        });

        it('skal få true dersom en datostring er gyldig', function () {
            var datoString = "01.01.2010";
            var erGyldig = erGyldigDato(datoString);

            expect(erGyldig).toBe(true);
        });

        it('skal få false dersom en datostring ikke er gyldig', function () {
            var datoString = "40.01.2010";
            var erGyldig = erGyldigDato(datoString);

            expect(erGyldig).toBe(false);
        });

        it('skal få at 29. februar ikke er gyldig dato dersom det ikke er skuddår', function () {
            var datoString = "29.02.2010";
            var erGyldig = erGyldigDato(datoString);

            expect(erGyldig).toBe(false);
        });

        it('skal få at 29. februar er gyldig dato dersom det er skuddår', function () {
            var datoString = "29.02.2012";
            var erGyldig = erGyldigDato(datoString);

            expect(erGyldig).toBe(true);
        });
    });

    describe('tall med 2 siffer', function () {
        it('skal få returverdi', function () {
            var tall = 1;

            expect(konverterTallTilStringMedToSiffer(tall)).toBeDefined();
        });

        it('skal legge til 0 dersom ett tall bare har ett siffer', function () {
            var tall = 1;

            expect(konverterTallTilStringMedToSiffer(tall)).toBe("0" + tall);
        });

        it('skal ikke legge til 0 dersom ett tall har 2 siffer', function () {
            var tall = 10;

            expect(konverterTallTilStringMedToSiffer(tall)).toBe("10");
        });
    });
});


