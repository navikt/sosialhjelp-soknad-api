describe('validering hjelpefunksjoner', function () {
    var metodeArray, attrs;

    describe('required', function () {
        var feilReturVerdi = 'required';

        beforeEach(function() {
            metodeArray = [];
        });

        it('validate skal legge til valideringsmetoden i array når attrs inneholder key required', function() {
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            expect(metodeArray.length).toBe(1);
        });

        it('skal kunne kalle valideringsmetoden som er lagt til array', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBeDefined();
        });

        it('skal få true dersom verdien inneholder tekst', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(true);
        });

        it('skal få nøkkel til feilmelding dersom verdien er en tom string', function() {
            var returVerdi;
            var verdi = "";
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });

        it('skal få nøkkel til feilmelding dersom verdien er undefined', function() {
            var returVerdi;
            var verdi;
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });

        it('skal få nøkkel til feilmelding dersom verdien er en string med bare whitespace', function() {
            var returVerdi;
            var verdi = "    ";
            attrs = {required: true};
            metodeArray.push(new RequiredValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });
    });

    describe('pattern', function() {
        var feilReturVerdi = 'pattern';
        var pattern = "/^[a-z]*$/";

        beforeEach(function() {
            metodeArray = [];
        });

        it('validate skal legge til valideringsmetoden i array når attrs inneholder key pattern', function() {
            attrs = {pattern: pattern};
            metodeArray.push(new PatternValidator(attrs));
            expect(metodeArray.length).toBe(1);
        });

        it('skal kunne kalle valideringsmetoden som er lagt til array', function() {
            var returVerdi;
            var verdi = "abc";
            attrs = {pattern: pattern};
            metodeArray.push(new PatternValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBeDefined();
        });

        it('skal få true dersom verdien passerer regexp-en', function() {
            var returVerdi;
            var verdi = "abc";
            attrs = {pattern: pattern};
            metodeArray.push(new PatternValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(true);
        });

        it('skal få nøkkel til feilmelding dersom verdien passerer regexp-en', function() {
            var returVerdi;
            var verdi = "123";
            attrs = {pattern: pattern};
            metodeArray.push(new PatternValidator(attrs));
            returVerdi = metodeArray[0].validate(verdi);
            expect(returVerdi).toBe(feilReturVerdi);
        });
    });

    describe('lengde', function() {

        beforeEach(function() {
            metodeArray = [];
        });

        describe('gitt bare minimumslengde', function() {
            var feilReturVerdi = 'minlength';

            it('validate skal legge til valideringsmetode i array når attrs inneholder key minlength', function() {
                attrs = {minlength: 3};
                metodeArray.push(new LengthValidator(attrs));
                expect(metodeArray.length).toBe(1);
            });

            it('skal kunne kalle valideringsmetoden for minimumslengde', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {minlength: 3};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBeDefined();
            });

            it('skal få true dersom verdi har like mange tegn som minimum', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {minlength: 3};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(true);
            });

            it('skal få nøkkel til feilmelding dersom verdi har mindre tegn enn minimum', function() {
                var returVerdi;
                var verdi = "ab";
                attrs = {minlength: 3};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(feilReturVerdi);
            });
        });

        describe('gitt bare maximumslengde', function() {
            var feilReturVerdi = 'maxlength';
            it('validate skal legge til valideringsmetode i array når attrs inneholder key maxlength', function() {
                attrs = {maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                expect(metodeArray.length).toBe(1);
            });

            it('skal kunne kalle valideringsmetoden for maximumslengde', function() {
                var returVerdi;
                var verdi = "abc";
                attrs = {maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBeDefined();
            });

            it('skal få true dersom verdi har like mange tegn som minimum', function() {
                var returVerdi;
                var verdi = "abcdef";
                attrs = {maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(true);
            });

            it('skal få nøkkel til feilmelding dersom verdi har mindre tegn enn minimum', function() {
                var returVerdi;
                var verdi = "abcdefg";
                attrs = {maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(feilReturVerdi);
            });
        });

        describe('gitt både minimumslengde og maximumslengde', function() {
            it('validate skal legge til begge valideringsmetodene i array når attrs inneholder både key maxlength og key minlength', function() {
                attrs = {minlength: 3, maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                expect(metodeArray.length).toBe(1);
            });

            it('skal kunne kalle begge valideringsmetodene for lengde', function() {
                var returVerdi1, returVerdi2;
                var verdi = "abc";
                attrs = {minlength: 3, maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));
                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(true);
            });

            it('skal få true fra begge metoder dersom antall tegn er mellom maximum og minimum', function() {
                var returVerdi;
                var verdi = "abcd";
                attrs = {minlength: 3, maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));

                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toBe(true);
            });
            it('skal få false hvis verdi er større enn maxlength', function() {
                var returVerdi;
                var verdi = "abcdefgh";
                attrs = {minlength: 3, maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));

                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toEqual('maxlength');
            });
            it('skal få false hvis verdi er større enn maxlength', function() {
                var returVerdi;
                var verdi = "ab";
                attrs = {minlength: 3, maxlength: 6};
                metodeArray.push(new LengthValidator(attrs));

                returVerdi = metodeArray[0].validate(verdi);
                expect(returVerdi).toEqual('minlength');
            });
        });

    });
});