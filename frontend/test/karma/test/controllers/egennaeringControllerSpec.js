(function () {
    'use strict';

    describe('EgennaeringController', function () {
        var scope, ctrl, form, event;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'sendsoknad.services'));
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
            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            scope.apneTab = function () {
            };
            scope.lukkTab = function () {
            };
            scope.settValidert = function () {
            };
            scope.leggTilStickyFeilmelding = function () {
            };

            scope.data = data;
            var foreldreFaktum = {
                key: 'egennaering.gardsbruk',
                value: 'false'
            };

            var gardsEier = {
                key: 'gardsEier',
                value: 'true'
            };

            var gardsEierJeg = {
                key: 'egennaering.gardsbruk.false.eier.jeg',
                value: 'true'
            };

            scope.data.leggTilFaktum(foreldreFaktum);
            scope.data.leggTilFaktum(gardsEier);
            scope.data.leggTilFaktum(gardsEierJeg);

            ctrl = $controller('EgennaeringCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.runValidationBleKalt).toEqual(false);
            scope.valider();
            expect(scope.runValidationBleKalt).toEqual(true);
        });

        it('skal generere aarstallene fra i år og 4 år bakover', function () {
            //ctrl.genererAarstallListe;
            expect(scope.aarstall.length).toEqual(5);
        });

        it('prevalgte aret skal være fjorårets år', function () {
            var idag = new Date();
            var ifjor = idag.getFullYear();
            expect(scope.forrigeAar).toEqual((ifjor - 1).toString());
        });
        it('skal vise slettknapp for orgnr2 og ikke for orgnr 1', function () {
            expect(scope.skalViseSlettKnapp(0)).toEqual(false);
            expect(scope.skalViseSlettKnapp(1)).toEqual(true);
        });
        it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
            spyOn(scope, "runValidation").andReturn(true);
            spyOn(scope, "lukkTab");
            spyOn(scope, "settValidert");
            scope.valider(false);
            expect(scope.runValidation).toHaveBeenCalledWith(false);
            expect(scope.lukkTab).toHaveBeenCalledWith('egennaering');
            expect(scope.settValidert).toHaveBeenCalledWith('egennaering');
        });
        it('skal apne bolken for invalid form', function () {
            spyOn(scope, "runValidation").andReturn(false);
            spyOn(scope, "apneTab");
            scope.valider(false);
            expect(scope.runValidation).toHaveBeenCalledWith(false);
            expect(scope.apneTab).toHaveBeenCalledWith('egennaering');
        });
        it('erSynlig skal returnere true for et faktum hvor foreldreelementet er svart pa slik at barneelementet blir synlig', function () {
            expect(scope.erSynlig('egennaering.gardsbruk')).toEqual(true);
        });
        it('erGardseier skal returnere true for et faktum hvis faktumet er huket av og faktumet egennaering.gardsbruk er synlig', function () {
            expect(scope.gardseier('gardsEier')).toEqual(true);
        });
        it('svartPaHvemEierGardsbuket skal returnere false dersom det bare er en eier av gårdsbruket', function () {
            expect(scope.svartPaHvemEierGardsbruket()).toEqual(false);
        });

        it('endreTypeGardsbruk skal sette harHuketAvTypeGardsbruk til true hvis ikke type gardsbruk er besvart', function () {
            var faktum = {
                key: 'egennaering.gardsbruk.false.eier.ektefelle',
                value: 'true'
            };
            scope.data.leggTilFaktum(faktum);
            expect(scope.svartPaHvemEierGardsbruket()).toEqual(true);
        });

        it('svartPaHvemEierGardsbuket skal returnere false hvis ingen har svart pa hvem som eier gardsbruket enda', function () {
            scope.data.slettFaktum('egennaering.gardsbruk');
            var foreldreFaktum = {
                key: 'egennaering.gardsbruk',
                value: 'true'
            };
            scope.data.leggTilFaktum(foreldreFaktum);
            expect(scope.svartPaHvemEierGardsbruket()).toEqual(false);
        });
        it('eierGardsbrukNokler skal vaere true nar eier av gardsbruket er besvart', function () {
            expect(scope.harHuketAvEierGardsbruk.value).toEqual(true);
        });
        it('endreTypeGardsbruk skal sette harHuketAvTypeGardsbruk til tom string hvis ikke type gardsbruk er besvart', function () {
            scope.endreTypeGardsbruk();
            expect(scope.harHuketAvTypeGardsbruk.value).toEqual('');
        });
        it('endreTypeGardsbruk skal sette harHuketAvTypeGardsbruk til true hvis ikke type gardsbruk er besvart', function () {
            var faktum = {
                key: 'egennaering.gardsbruk.false.type.dyr',
                value: 'true'
            };
            scope.data.leggTilFaktum(faktum);
            scope.endreTypeGardsbruk();
            expect(scope.harHuketAvTypeGardsbruk.value).toEqual(true);
        });
        it('endreEierGardsbruk  skal sette harHuketAvEierGardsbruk til true hvis ikke type gardsbruk er besvart', function () {
            scope.endreEierGardsbruk();
            expect(scope.harHuketAvEierGardsbruk.value).toEqual(true);
        });
        it('endreEierGardsbruk  skal sette harHuketAvEierGardsbruk til tom string hvis ikke type gardsbruk er besvart', function () {
            var gardsEierJeg = {
                key: 'egennaering.gardsbruk.false.eier.jeg',
                value: 'false'
            };

            scope.data.leggTilFaktum(gardsEierJeg);
            scope.endreEierGardsbruk();
            expect(scope.harHuketAvEierGardsbruk.value).toEqual('');
        });
        it('prosentFeil skal returnere true nar prosentfeil skal vises', function () {
            scope.summererAndeleneTil100();
            expect(scope.prosentFeil()).toEqual(true);
        });
        it('prosentFeil skal returnere false nar prosentfeil ikke skal vises', function () {
            var gardsEierJeg = {
                key: 'egennaering.gardsbruk.false.eierandel.din',
                value: '100'
            };
            scope.data.leggTilFaktum(gardsEierJeg);
            scope.summererAndeleneTil100();
            expect(scope.prosentFeil()).toEqual(false);
        });
        it('skal summere de ulike eierne til 100 og ikke gi feil dersom dette stemmer', function () {
            var gardsEierErEktefelle = {
                key: 'egennaering.gardsbruk.false.eier.ektefelle',
                value: 'true'
            };
            scope.data.leggTilFaktum(gardsEierErEktefelle);

            var gardsEierErAnnet = {
                key: 'egennaering.gardsbruk.false.eier.annet',
                value: 'true'
            };
            scope.data.leggTilFaktum(gardsEierErAnnet);

            var gardsEierJeg = {
                key: 'egennaering.gardsbruk.false.eierandel.din',
                value: '33'
            };
            scope.data.leggTilFaktum(gardsEierJeg);
            var gardsEierEktefelle = {
                key: 'egennaering.gardsbruk.false.eierandel.ektefelle',
                value: '34'
            };
            scope.data.leggTilFaktum(gardsEierEktefelle);

            var gardsEierAnnet = {
                key: 'egennaering.gardsbruk.false.eierandel.annet',
                value: '33'
            };
            scope.data.leggTilFaktum(gardsEierAnnet);

            scope.summererAndeleneTil100();
            expect(scope.prosentFeil()).toEqual(false);
        });
    });
}());