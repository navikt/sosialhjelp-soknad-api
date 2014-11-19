describe('utility funksjoner -', function () {
    describe('Array prototype:', function () {

        var array, objArray, complexObjArray;

        beforeEach(function () {
            array = [1, 2, 3];
            objArray = [
                {a: 1, 2: 'b'}
            ];

            complexObjArray = [
                {
                    field1: 1,
                    field2: 2
                },
                {
                    field1: 2,
                    field2: 2
                }
            ];
        });

        it('skal returnere indexen til et element dersom det finnes', function() {
            expect(array.indexOf(2)).toBe(1);
        });

        it('skal returnere -1 dersom det ikke finnes', function() {
            expect(array.indexOf(4)).toBe(-1);
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

        it('skal returnere index til første element med gitt verdi ved gitt attributt-navn', function () {
            expect(complexObjArray.indexByFieldValue('field1', 1)).toBe(0);
            expect(complexObjArray.indexByFieldValue('field1', 2)).toBe(1);
            expect(complexObjArray.indexByFieldValue('field2', 2)).toBe(0);
        });

        it('skal returnere -1 dersom ingen element har gitt verdi ved gitt attributt', function () {
            expect(complexObjArray.indexByFieldValue('field1', 3)).toBe(-1);
        });
    });

    describe('String prototype', function() {
        it('Skal konvertere string som består av bare store bokstaver og underscore til camelcase', function () {
            var expectedString = "camelCase";
            var originalString = "CAMEL_CASE";

            expect(originalString.toCamelCase()).toBe(expectedString);
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

    describe('Sjekk attributt av java-objekt', function () {
        it('skal returnere true for objekt som inneholder attributt uten angular-prefix', function () {
            var obj = {attributt: 1};

            expect(harAttributt(null, obj, 'attributt')).toBe(1);
        });

        it('skal returnere false for objekt som ikke inneholder attributt uten angular-prefix', function () {
            var obj = {attributt: 1};

            expect(harAttributt(null, obj, 'blah')).toBe(false);
        });

        it('skal returnere true for objekt som inneholder attributt med angular-prefix men uten data-prefix', function () {
            var obj = {'ngAttributt': 1};

            expect(harAttributt(null, obj, 'attributt')).toBe(1);
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

        it('skal returnere true for fremtidig dato', function() {
            var imorgen = new Date();
            imorgen.setDate(imorgen.getDate() + 1);

            var result = erFremtidigDato(imorgen.getFullYear(), imorgen.getMonth()+1, imorgen.getDate());
            expect(result).toEqual(true);
        });

        it('skal returnere false for ikke fremtidig dato', function() {
            var idag = new Date();
            
            var result = erFremtidigDato(idag.getFullYear(), idag.getMonth()+1, idag.getDate());
            expect(result).toBe(false);
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

    describe('lage norsk datoformat(dd.MM.YYYY) fra iso-standard(YYYY-MM-dd)', function() {
        it('skal returnere 12.12.2014 om vi sender inn 2014-12-12', function() {
            expect(lagNorskDatoformatFraIsoStandard("2014-12-12")).toBe("12.12.2014");
        });

        it('skal legge til 0 dager som har kun et siffer', function() {
           expect(lagNorskDatoformatFraIsoStandard("2012-11-01")).toBe("01.11.2012");
        });

        it('skal legge til 0 på dager som har kun et siffer', function() {
            expect(lagNorskDatoformatFraIsoStandard("2012-01-11")).toBe("11.01.2012");
        });

        it('skal returnere null ved ugyldig dato', function() {
            expect(lagNorskDatoformatFraIsoStandard("2013-55-32")).toBeNull();
        });

        it('skal kun godta dato på ISO-format', function() {
           expect(lagNorskDatoformatFraIsoStandard("12-12-2014")).toBeNull();
        });
    });
});


