describe('Personalia directive tests', function () {
    var element, scope;

    beforeEach(module('nav.personalia', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        var fakta = [
            {}
        ];

        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
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
                "soknad.brukerprofil.url": "brukerprofilUrl"
            }
        });
    }));

    describe('personalia directive', function () {
        beforeEach(inject(function ($compile, $rootScope, data) {
            var faktum = {
                key: 'personalia',
                properties: {
                    alder: "61"
                }
            };
            data.leggTilFaktum(faktum);

            element = angular.element('<div nav-personalia></div>');
            $compile(element)($rootScope);
            $rootScope.$apply();
            scope = element.find('div').scope();
        }));

        it('hvis personen ikke har et kjønn så skal erMann returnere false', function () {
            expect(scope.erMann()).toEqual(false);
        });
        it('hvis personen ikke har et kjønn så skal erKvinne returnere false', function () {
            expect(scope.erKvinne()).toEqual(false);
        });
    });
});


