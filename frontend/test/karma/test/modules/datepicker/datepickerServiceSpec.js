describe('datepicker services', function () {
    beforeEach(module('nav.datepicker.service'));

    describe('dateService', function () {
        var injectedDateService;
        beforeEach(inject(function (dateService) {
            injectedDateService = dateService;
        }));

        describe('isValidDate', function() {
            it('01.01.2000 skal være gyldig dato', function() {
                expect(injectedDateService.isValidDate('01.01.2000')).toBe(true);
            });

            it('31.01.2000 skal være gyldig dato', function() {
                expect(injectedDateService.isValidDate('31.01.2000')).toBe(true);
            });

            it('15.01.2000 skal være gyldig dato', function() {
                expect(injectedDateService.isValidDate('15.01.2000')).toBe(true);
            });

            it('28.02.2014 skal være gyldig dato', function() {
                expect(injectedDateService.isValidDate('28.02.2014')).toBe(true);
            });

            it('29.02.2012 skal være gyldig dato', function() {
                expect(injectedDateService.isValidDate('29.02.2012')).toBe(true);
            });

            it('30.02.2012 skal være ikke gyldig dato', function() {
                expect(injectedDateService.isValidDate('30.02.2012')).toBe(false);
            });

            it('10.13.2012 skal være ikke gyldig dato', function() {
                expect(injectedDateService.isValidDate('10.13.2012')).toBe(false);
            });

            it('10.00.2012 skal være ikke gyldig dato', function() {
                expect(injectedDateService.isValidDate('10.00.2012')).toBe(false);
            });
        });

        describe('reverseNorwegianDateFormat', function() {
            it('01.05.2000 skal bli formatert til 2000-05-01', function() {
                expect(injectedDateService.reverseNorwegianDateFormat('01.05.2000')).toBe('2000-05-01');
            });
        });

        describe('hasCorrectDateFormat', function() {
            it('01.01.2000 skal være gyldig datoformat', function() {
                expect(injectedDateService.hasCorrectDateFormat('01.01.2000')).toBe(true);
            });

            it('01.01.200 skal være ikke gyldig datoformat', function() {
                expect(injectedDateService.hasCorrectDateFormat('01.01.200')).toBe(false);
            });

            it('1.01.2000 skal være ikke gyldig datoformat', function() {
                expect(injectedDateService.hasCorrectDateFormat('1.01.2000')).toBe(false);
            });

            it('01.1.2000 skal være ikke gyldig datoformat', function() {
                expect(injectedDateService.hasCorrectDateFormat('01.1.2000')).toBe(false);
            });

//            describe('isFutureDate', function() {
//                it('', function() {
//                    expect(injectedDateService.reverseNorwegianDateFormat('01.05.2000')).toBe('2000-05-01');
//                });
//            });
        });
    });
});