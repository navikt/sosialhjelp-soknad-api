(function () {
    'use strict';

    describe('AdresseCtrl', function () {
        var scope, ctrl, form, event;
        event = $.Event("click");

        beforeEach(module('app.services'));
        beforeEach(module('app.controllers', 'nav.feilmeldinger'));

        beforeEach(module(function ($provide) {
            var fakta = [
                {}
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
                },
                soknad: {
                    soknadId: 1
                },
                slettFaktum: function (faktumData) {
                    fakta.forEach(function (item, index) {
                        if (item.faktumId === faktumData.faktumId) {
                            fakta.splice(index, 1);
                        }
                    });
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
        })
        )
        ;

        beforeEach(inject(function ($injector, $rootScope, $controller, data) {
            scope = $rootScope;

            scope.data = data;
            var faktum = {
                key: 'personalia',
                properties: {
                    alder: "61"
                }
            };
            scope.data.leggTilFaktum(faktum);

            scope.personalia = {
                gjeldendeAdresse: "Gjeldene adresse"
            };
            ctrl = $controller('AdresseCtrl', {
                $scope: scope

            });
        }));

        it('Skal returnere true hvis har gjeldene adresse', function () {
            //scope.personalia = {gjeldendeAdresse: "Gjeldene adresse"};
            expect(scope.harGjeldendeAdresse()).toEqual(true);
        });
        it('Skal returnere true hvis har sekundær adresse', function () {
            scope.personalia = {gjeldendeAdresse: "Gjeldene adresse", sekundarAdresse: 'sekundær adresse'};
            expect(scope.harGjeldendeAdresse()).toEqual(true);
        });
        it('adressen skal returneres på adresseformatet', function () {
            var adresse = "Gatenavn 1, Poststed 0000";
            var formatertAdresse = '<p>Gatenavn 1</p><p>Poststed 0000</p>';
            expect(scope.hentFormattertAdresse(adresse)).toEqual(formatertAdresse);
        });
        it('formatertAdresse med ingen adresse skal returnere tom streng', function () {
            expect(scope.hentFormattertAdresse()).toEqual('');
        });
        it('adressetype BOSTEDSADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("BOSTEDSADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype UTENLANDSK_ADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("UTENLANDSK_ADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype POSTADRESSE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("POSTADRESSE")).toEqual("personalia.folkeregistrertadresse");
        });
        it('adressetype MIDLERTIDIG_POSTADRESSE_NORGE skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("MIDLERTIDIG_POSTADRESSE_NORGE")).toEqual("personalia.midlertidigAdresseNorge");
        });
        it('adressetype MIDLERTIDIG_POSTADRESSE_UTLAND skal returnere folkeregistrertadresse ', function () {
            expect(scope.hentAdresseTypeNokkel("MIDLERTIDIG_POSTADRESSE_UTLAND")).toEqual("personalia.midlertidigAdresseUtland");
        });
        it('ugyldig adressetype skal returnere tom string ', function () {
            expect(scope.hentAdresseTypeNokkel("sdfsdf")).toEqual("");
        });
    });
}());