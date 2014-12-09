describe('datepicker services', function () {
    beforeEach(module('nav.datepicker.service'));

    describe('dateService', function () {
        var service;
        var OldDate = Date;
        var today = 1399268154501;
        var idagSomString = '05.05.2014';
        beforeEach(inject(function (dateService) {
            service = dateService;

            Date = function(time) {
                if (time === undefined) {
                    return new OldDate(today);
                }
                return new OldDate(time);
            };
        }));

        afterEach(function() {
            Date = OldDate;
        });

        describe('isValidDate', function() {
            it('01.01.2000 skal være gyldig dato', function() {
                expect(service.isValidDate('01.01.2000')).toBe(true);
            });

            it('31.01.2000 skal være gyldig dato', function() {
                expect(service.isValidDate('31.01.2000')).toBe(true);
            });

            it('15.01.2000 skal være gyldig dato', function() {
                expect(service.isValidDate('15.01.2000')).toBe(true);
            });

            it('28.02.2014 skal være gyldig dato', function() {
                expect(service.isValidDate('28.02.2014')).toBe(true);
            });

            it('29.02.2012 skal være gyldig dato', function() {
                expect(service.isValidDate('29.02.2012')).toBe(true);
            });

            it('30.02.2012 skal være ikke gyldig dato', function() {
                expect(service.isValidDate('30.02.2012')).toBe(false);
            });

            it('10.13.2012 skal være ikke gyldig dato', function() {
                expect(service.isValidDate('10.13.2012')).toBe(false);
            });

            it('10.00.2012 skal være ikke gyldig dato', function() {
                expect(service.isValidDate('10.00.2012')).toBe(false);
            });
        });

        describe('reverseNorwegianDateFormat', function() {
            it('01.05.2000 skal bli formatert til 2000-05-01', function() {
                expect(service.reverseNorwegianDateFormat('01.05.2000')).toBe('2000-05-01');
            });
        });

        describe('hasCorrectDateFormat', function() {
            it('01.01.2000 skal være gyldig datoformat', function() {
                expect(service.hasCorrectDateFormat('01.01.2000')).toBe(true);
            });

            it('01.01.200 skal være ikke gyldig datoformat', function() {
                expect(service.hasCorrectDateFormat('01.01.200')).toBe(false);
            });

            it('1.01.2000 skal være ikke gyldig datoformat', function() {
                expect(service.hasCorrectDateFormat('1.01.2000')).toBe(false);
            });

            it('01.1.2000 skal være ikke gyldig datoformat', function() {
                expect(service.hasCorrectDateFormat('01.1.2000')).toBe(false);
            });

            describe('isFutureDate', function() {
                it('En dag som har vært skal returnere false', function() {
                    expect(service.isFutureDate('04.05.2014')).toBe(false);
                });

                it('En dag som var forrige måned skal returnere false', function() {
                    expect(service.isFutureDate('05.04.2014')).toBe(false);
                });

                it('En dag som har vært for lenge siden skal returnere false', function() {
                    expect(service.isFutureDate('04.05.1014')).toBe(false);
                });

                it('I dag skal returnere false', function() {
                    expect(service.isFutureDate(idagSomString)).toBe(false);
                });

                it('I morgen skal returnere true', function() {
                    expect(service.isFutureDate('06.05.2014')).toBe(true);
                });

                it('En dag neste måned skal returnere true', function() {
                    expect(service.isFutureDate('05.06.2014')).toBe(true);
                });
            });
        });
    });

    describe('maskService', function () {
        var service;
        var initialMaskText = 'dd.mm.yyyy';

        beforeEach(module(function ($provide) {
            $provide.value('cmsService', {
                getTrustedHtml: function() {
                    return initialMaskText;
                }
            });
        }));

        beforeEach(inject(function (maskService) {
            service = maskService;
        }));

        describe('getMaskText', function() {
            it('skal få initialMaskText ved tom string', function() {
                expect(service.getMaskText('')).toBe(initialMaskText);
            });

            it('skal få d.mm.yyyy ved string med ett for dag', function() {
                expect(service.getMaskText('1')).toBe('d.mm.yyyy');
            });

            it('skal få .mm.yyyy ved string med to tall for dag', function() {
                expect(service.getMaskText('12')).toBe('.mm.yyyy');
            });

            it('skal få tom string ved 3 tall for dag', function() {
                expect(service.getMaskText('123')).toBe('');
            });

            it('skal få mm.yyyy ved rett dag-format og ett punktum', function() {
                expect(service.getMaskText('12.')).toBe('mm.yyyy');
            });

            it('skal få tom string ved dato så 2 punktum', function() {
                expect(service.getMaskText('12..')).toBe('');
            });

            it('skal få m.yyyy ved rett dag-format og ett tall for måned', function() {
                expect(service.getMaskText('12.1')).toBe('m.yyyy');
            });

            it('skal få tom string ved rett dag-format og galt månedformat ', function() {
                expect(service.getMaskText('12.123')).toBe('');
            });

            it('skal få yyy ved rett dag- og måned-format, og ett tall for år', function() {
                expect(service.getMaskText('12.12.1')).toBe('yyy');
            });
        });
    });

    describe('datepickerInputService', function () {
        var service;
        var periodKeyCode = 190;
        var inputKeys = [1,2,3,4,5,6, periodKeyCode];
        var utilityKeys = [7, 8, 9];
        var maxLength = 10;

        beforeEach(module(function ($provide) {
            $provide.value('datepickerInputKeys', inputKeys);
            $provide.value('datepickerUtilityKeys', utilityKeys);
            $provide.value('periodKeyCode', 190);
            $provide.value('cKeyCode', 67);
            $provide.value('vKeyCode', 86);
        }));

        beforeEach(inject(function (datepickerInputService) {
            service = datepickerInputService;
        }));

        describe('isValidInput', function() {
            it('gyldig input-key og under maks-lengde skal returnere true', function() {
                var event = {
                    keyCode: 1,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, 0, maxLength, 0, false)).toBe(true);
            });

            it('gyldig ikke input-key og under maks-lengde skal returnere true', function() {
                var event = {
                    keyCode: 7,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, 0, maxLength, 0, false)).toBe(true);
            });

            it('ikke gyldig key skal returnere false', function() {
                var event = {
                    keyCode: 100,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, 0, maxLength, 0, false)).toBe(false);
            });

            it('gyldig input-key men lengden er allerede på maks skal gi false', function() {
                var event = {
                    keyCode: 1,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, maxLength, maxLength, 0, false)).toBe(false);
            });

            it('gyldig ikke input-key med lengde som allerede er på maks skal gi true', function() {
                var event = {
                    keyCode: 7,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, maxLength, maxLength, 0, false)).toBe(true);
            });

            it('punktum skal bare være gyldig dersom caret er ved index 2 eller 5', function() {
                var event = {
                    keyCode: periodKeyCode,
                    ctrlKey: false
                };
                expect(service.isValidInput(event, 0, maxLength, 2, false)).toBe(true);
                expect(service.isValidInput(event, 0, maxLength, 5, false)).toBe(true);
                expect(service.isValidInput(event, 0, maxLength, 6, false)).toBe(false);
            });
        });

        describe('addPeriodAtRightIndex', function() {
            it('skal sette inn punktum ved index 3 og 6', function() {
                expect(service.addPeriodAtRightIndex('10', '1', 2)[0]).toBe('10.');
                expect(service.addPeriodAtRightIndex('10.10', '10.1', 5)[0]).toBe('10.10.');
            });

            it('skal ikke sette inn punktum ved index annet enn 3 og 6', function() {
                expect(service.addPeriodAtRightIndex('1', '', 1)[0]).toBe('1');
                expect(service.addPeriodAtRightIndex('10.1', '10.', 4)[0]).toBe('10.1');
            });

            it('skal ikke sette inn punktum dersom det allerede er punktum ved index 3 og 6', function() {
                expect(service.addPeriodAtRightIndex('10.', '10', 3)[0]).toBe('10.');
                expect(service.addPeriodAtRightIndex('10.10.', '10.10', 6)[0]).toBe('10.10.');
            });
        });
    });
});