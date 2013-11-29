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

    describe('konverter dato representert som string til date-objekt', function () {
        it('skal få returverdi', function () {
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("");
            expect(dato).toBeDefined();
        });

        it('skal få tom string dersom datostringen er på galt format', function () {
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("");
            expect(dato).toBe("");
        });

        it('skal få tilbake dato-objekt dersom datostringen er på rett format', function () {
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("01.01.2013");
            expect(dato.getDate()).toBeDefined();
        });

        it('skal få tilbake dato-objekt med rett dag', function () {
            var forventetDag = 1;
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("01.01.2013");
            expect(dato.getDate()).toBe(forventetDag);
        });

        it('skal få tilbake dato-objekt med rett måned', function () {
            var forventetManed = 0;
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("01.01.2013");
            expect(dato.getMonth()).toBe(forventetManed);
        });

        it('skal få tilbake dato-objekt med rett år', function () {
            var forventetAr = 2013;
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("01.01.2013");
            expect(dato.getFullYear()).toBe(forventetAr);
        });

        it('skal få tilbake dato-objekt med rett dato satt', function () {
            var forventetDag = 10;
            var forventetManed = 4;
            var forventetAr = 2011;
            var dato = konverterStringFraNorskDatoformatTilDateObjekt("10.05.2011");
            expect(dato.getDate()).toBe(forventetDag);
            expect(dato.getMonth()).toBe(forventetManed);
            expect(dato.getFullYear()).toBe(forventetAr);
        });
    });
});


