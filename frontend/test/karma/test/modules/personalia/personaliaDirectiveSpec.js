describe('Personalia directive tests', function () {
    var element, scope;
    var brukerprofilUrl = 'brukerprofil'

    beforeEach(module('nav.personalia', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {}
        ];

        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
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
            },
            slettFaktum: function (faktumData) {
                fakta.forEach(function (item, index) {
                    if (item.faktumId === faktumData.faktumId) {
                        fakta.splice(index, 1);
                    }
                });
            },
            config: {
                "soknad.brukerprofil.url": brukerprofilUrl
            }
        });
    }));

    describe('personalia directive', function () {
        beforeEach(inject(function ($compile, $rootScope, data) {
            var faktum = {
                key: 'personalia',
                properties: {
                    alder: "61",
                    statsborgerskap: 'NOR'
                }
            };
            data.leggTilFaktum(faktum);

            element = angular.element('<div nav-personalia></div>');
            $compile(element)($rootScope);
            $rootScope.$apply();
            scope = element.find('div').scope();
        }));

        it('dersom personalia er hentet skal harHentetPersonalia returnere true', function () {
            expect(scope.harHentetPersonalia()).toEqual(true);
        });

        it('brukerprofilUrl skal bli satt til riktig url', function () {
            expect(scope.brukerprofilUrl).toBe(brukerprofilUrl);
        });

        it('dersom statsborgerskap er norsk så skal erUtenlandskStatsborger returnere false', function () {
            expect(scope.erUtenlandskStatsborger()).toEqual(false);
        });

        describe('ingen kjønn satt i personalia', function() {
            it('hvis personen ikke har et kjønn så skal erMann returnere false', function () {
                expect(scope.erMann()).toEqual(false);
            });
            it('hvis personen ikke har et kjønn så skal erKvinne returnere false', function () {
                expect(scope.erKvinne()).toEqual(false);
            });
        });

        describe('kjønn er satt til kvinne', function() {
            beforeEach(function () {
                scope.personalia.kjonn = 'k';
            });

            it('hvis personen er en kvinne så skal erMann returnere false', function () {
                expect(scope.erMann()).toEqual(false);
            });
            it('hvis personen er en kvinne så skal erKvinne returnere true', function () {
                expect(scope.erKvinne()).toEqual(true);
            });
        });

        describe('kjønn er satt til kvinne', function() {
            beforeEach(function () {
                scope.personalia.kjonn = 'm';
            });

            it('hvis personen er en mann så skal erMann returnere true', function () {
                expect(scope.erMann()).toEqual(true);
            });
            it('hvis personen er en mann så skal erKvinne returnere false', function () {
                expect(scope.erKvinne()).toEqual(false);
            });
        });
    });
});


