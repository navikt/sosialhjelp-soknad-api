describe('validering hjelpefunksjoner', function () {
    var metodeArray, attrs;

    describe('required', function () {
        var feilReturVerdi = 'required';

        beforeEach(function() {
            metodeArray = [];
        });

        it('init skal legge til valideringsmetoden i array når attrs inneholder key required', function() {
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            expect(metodeArray.length).toBe(1);
        });

        it('init skal ikke legge til noe i array når attrs ikke inneholder key required', function() {
            attrs = {};
            RequiredValidator.init(attrs, metodeArray);
            expect(metodeArray.length).toBe(0);
        });

        it('skal kunne kalle valideringsmetoden som er lagt til array', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBeDefined();
        });

        it('skal få true dersom verdien inneholder tekst', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(true);
        });

        it('skal få nøkkel til feilmelding dersom verdien er en tom string', function() {
            var returVerdi;
            var verdi = "";
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });

        it('skal få nøkkel til feilmelding dersom verdien er undefined', function() {
            var returVerdi;
            var verdi;
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });

        it('skal få nøkkel til feilmelding dersom verdien er en string med bare whitespace', function() {
            var returVerdi;
            var verdi = "    ";
            attrs = {required: true};
            RequiredValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });
    });

    describe('pattern', function() {
        var feilReturVerdi = 'pattern';
        var pattern = "/^[a-z]*$/";

        beforeEach(function() {
            metodeArray = [];
        });

        it('init skal legge til valideringsmetoden i array når attrs inneholder key pattern', function() {
            attrs = {pattern: pattern};
            PatternValidator.init(attrs, metodeArray);
            expect(metodeArray.length).toBe(1);
        });

        it('init skal ikke legge til noe i array når attrs ikke inneholder key pattern', function() {
            attrs = {};
            PatternValidator.init(attrs, metodeArray);
            expect(metodeArray.length).toBe(0);
        });

        it('skal kunne kalle valideringsmetoden som er lagt til array', function() {
            var returVerdi;
            var verdi = "abc";
            attrs = {pattern: pattern};
            PatternValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBeDefined();
        });

        it('skal få true dersom verdien passerer regexp-en', function() {
            var returVerdi;
            var verdi = "abc";
            attrs = {pattern: pattern};
            PatternValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(true);
        });

        it('skal få nøkkel til feilmelding dersom verdien passerer regexp-en', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {pattern: pattern};
            PatternValidator.init(attrs, metodeArray);
            returVerdi = metodeArray[0](verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });
    });

    describe('lengde', function() {

        beforeEach(function() {
            metodeArray = [];
        });

        it('init skal ikke legge til noe i array når attrs ikke inneholder verken key maxlength eller key minlength', function() {
            attrs = {};
            LengthValidator.init(attrs, metodeArray);
            expect(metodeArray.length).toBe(0);
        });

        describe('gitt bare minimumslengde', function() {
            var feilReturVerdi = 'minlength';

            it('init skal legge til valideringsmetode i array når attrs inneholder key minlength', function() {
                attrs = {minlength: 3};
                LengthValidator.init(attrs, metodeArray);
                expect(metodeArray.length).toBe(1);
            });

            it('skal kunne kalle valideringsmetoden for minimumslengde', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {minlength: 3};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBeDefined();
            });

            it('skal få true dersom verdi har like mange tegn som minimum', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {minlength: 3};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBe(true);
            });

            it('skal få nøkkel til feilmelding dersom verdi har mindre tegn enn minimum', function() {
                var returVerdi;
                var verdi = "ab";
                attrs = {minlength: 3};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBe(feilReturVerdi);
            });
        });

        describe('gitt bare maximumslengde', function() {
            var feilReturVerdi = 'maxlength';
            it('init skal legge til valideringsmetode i array når attrs inneholder key maxlength', function() {
                attrs = {maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                expect(metodeArray.length).toBe(1);
            });

            it('skal kunne kalle valideringsmetoden for maximumslengde', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBeDefined();
            });

            it('skal få true dersom verdi har like mange tegn som minimum', function() {
                var returVerdi;
                var verdi = "abcdef";
                attrs = {maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBe(true);
            });

            it('skal få nøkkel til feilmelding dersom verdi har mindre tegn enn minimum', function() {
                var returVerdi;
                var verdi = "abcdefg";
                attrs = {maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBe(feilReturVerdi);
            });
        });

        describe('gitt både minimumslengde og maximumslengde', function() {
            it('init skal legge til begge valideringsmetodene i array når attrs inneholder både key maxlength og key minlength', function() {
                attrs = {minlength: 3, maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                expect(metodeArray.length).toBe(2);
            });

            it('skal kunne kalle begge valideringsmetodene for lengde', function() {
                var returVerdi1, returVerdi2;
                var verdi = "abc";
                attrs = {minlength: 3, maxlength: 6};
                LengthValidator.init(attrs, metodeArray);
                returVerdi1 = metodeArray[0](verdi);
                returVerdi2 = metodeArray[1](verdi);
                expect(returVerdi1).toBeDefined();
                expect(returVerdi2).toBeDefined();
            });

            it('skal få true fra begge metoder dersom antall tegn er mellom maximum og minimum', function() {
                var returVerdi;
                var verdi = "abcd";
                attrs = {minlength: 3, maxlength: 6};
                LengthValidator.init(attrs, metodeArray);

                returVerdi = metodeArray[0](verdi);
                expect(returVerdi).toBe(true);

                returVerdi = metodeArray[1](verdi);
                expect(returVerdi).toBe(true);
            });
        });

    });
});