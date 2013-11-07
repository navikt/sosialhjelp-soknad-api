describe('utility funksjoner -', function () {

    describe('Array prototype:', function () {

        var array;

        beforeEach(function() {
            array = [1,2,3];
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

        beforeEach(function() {
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
});


