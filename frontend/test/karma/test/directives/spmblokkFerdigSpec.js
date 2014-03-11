describe('sporsmalferdig', function () {
    var rootScope, element, scope, timeout, form, event;
    event = $.Event("click");

    beforeEach(module('nav.sporsmalferdig', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'bolker',
                value: "",
                properties: {
                    enide: 'false'
                },
                $save: function () {
                }
            }
        ];

        $provide.value("data", {
            fakta: fakta,
            finnFaktum: function (key) {
                var res = null;
                fakta.forEach(function (item) {
                    if (item.key == key) {
                        res = item;
                    }
                });
                return res;
            },
            finnFakta: function (key) {
                var res = [];
                fakta.forEach(function (item) {
                    if (item.key === key) {
                        res.push(item);
                    }
                });
                return res;
            },
            leggTilFaktum: function (faktum) {
                fakta.push(faktum);
            }
        });
        $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
            'hjelpetekst.tekst': 'Hjelpetekst tekst' }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="form">' +
                '<div class="accordion-group" id="enide">' +
                '<div data-spmblokkferdig></div> ' +
                '</div>' +
                '</form>');

        scope.valider = function (key) {
        };
        scope.leggTilValideringsmetode = function (ke1, key2) {
        };

        $compile(element)(scope);
        scope.$apply();
    }));

    describe('spmblokkferdig', function () {
        it('knappTekst skal bli satt til neste', function () {
            expect(scope.knappTekst).toBe("neste");
        });
        it('bolken blir validert uten at det har skjedd en endring skal knappen fortsatt ha teksten neste', function () {
            var accordionGroup = element.find(".accordion-group");
            accordionGroup.addClass('validert');
            expect(scope.knappTekst).toBe('neste');
        });

        it('bolken blir var validert men så skjedde det en endring, skal knappen ha teksten lagreEndring', function () {
            var accordionGroup = element.find(".accordion-group");
            accordionGroup.addClass('validert');
            scope.$apply();
            expect(scope.knappTekst).toBe('neste');
            accordionGroup.removeClass('validert');
            scope.$apply();

            expect(scope.knappTekst).toBe('lagreEndring');

        });
        it('validerOgGaTilNeste', function () {
            spyOn(scope, 'valider');
            scope.validerOgGaaTilNeste();
            expect(scope.valider).toHaveBeenCalledWith(true);
        });
        it('validerOgGaTilNeste', function () {
            var accordionGroup = element.find(".accordion-group");
            expect(accordionGroup.hasClass('validert')).toBe(false);
            scope.validerOgGaaTilNeste();
            expect(accordionGroup.hasClass('validert')).toBe(true);
        });
        it('knappTekst er lagreEndring og validerOgGaTilNeste, skal knappTekst endres tilbake til neste', function () {
            var accordionGroup = element.find(".accordion-group");
            accordionGroup.addClass('validert');
            scope.$apply();
            expect(scope.knappTekst).toBe('neste');
            accordionGroup.removeClass('validert');
            scope.$apply();

            expect(accordionGroup.hasClass('validert')).toBe(false);
            scope.validerOgGaaTilNeste();
            expect(accordionGroup.hasClass('validert')).toBe(true);
            scope.$apply();

            expect(scope.knappTekst).toBe('neste')
        });
    });
});
describe('sporsmalferdig', function () {
    var rootScope, element, scope, timeout, form, event;
    event = $.Event("click");

    beforeEach(module('nav.sporsmalferdig', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {key: 'bolker',
                value: "",
                properties: {
                    enide: 'false'
                },
                $save: function () {
                }
            }
        ];

        $provide.value("data", {
            fakta: fakta,
            finnFaktum: function (key) {
                var res = null;
                fakta.forEach(function (item) {
                    if (item.key == key) {
                        res = item;
                    }
                });
                return res;
            },
            finnFakta: function (key) {
                var res = [];
                fakta.forEach(function (item) {
                    if (item.key === key) {
                        res.push(item);
                    }
                });
                return res;
            },
            leggTilFaktum: function (faktum) {
                fakta.push(faktum);
            }
        });
        $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
            'hjelpetekst.tekst': 'Hjelpetekst tekst' }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="form">' +
                    '<div class="accordion-group" id="enide">' +
                        '<div data-spmblokkferdig></div> ' +
                    '</div>' +
                    '<div class="accordion-group" id="toide">' +
                    '</div>' +
            '</form>');

        scope.valider = function (key) {
        };
        scope.apneTab = function (key) {
        };
        scope.leggTilValideringsmetode = function (ke1, key2) {
        };

        $compile(element)(scope);
        scope.$apply();
    }));

    describe('spmblokkferdig', function () {
        it('apneTab skal bli kalt når første bolk er validert, formen er valid, og andre bolk har ikke klassen validert, og validerOgGaaTilNeste kalles', function () {
            spyOn(scope, 'apneTab');
            var accordionGroup = element.find(".accordion-group").first();
            var accordionGroup2 = element.find(".accordion-group").last();

            accordionGroup.addClass('validert');
            scope.$apply();
            expect(scope.knappTekst).toBe('neste');
            accordionGroup.removeClass('validert');
            scope.$apply();
            expect(scope.knappTekst).toBe('lagreEndring');

            expect(accordionGroup2.hasClass('validert')).toBe(false);

            accordionGroup.find('button').first().click();
            scope.$apply();
            expect(scope.apneTab).toHaveBeenCalled();
        });
    });
});
