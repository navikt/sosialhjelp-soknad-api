/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('DagpengerControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'app.services'));
        beforeEach(module('app.controllers', 'nav.feilmeldinger'));

        beforeEach(module(function ($provide) {
            var fakta = [
                {
                    key: 'personalia',
                    properties: {
                        alder: "61"
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
                },
                land: 'Norge',
                soknad: {
                    soknadId: 1
                },
                config: {"soknad.sluttaarsak.url": "", "soknad.lonnskravskjema.url": "", "soknad.permitteringsskjema.url":"", "minehenvendelser.link.url": "minehenvendelserurl", "soknad.inngangsporten.url": "inngangsportenurl" },
                slettFaktum: function (faktumData) {
                    fakta.forEach(function (item, index) {
                        if (item.faktumId === faktumData.faktumId) {
                            fakta.splice(index, 1);
                        }
                    });
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
            $provide.value("$routeParams", {});
        })
        )
        ;

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.expectGET('../js/app/directives/feilmeldinger/feilmeldingerTemplate.html').
                respond('');

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

            element = angular.element(
                '<form name="form">' +
                    '<div form-errors></div>' +
                    '<input type="text" ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                    '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>' +
                    '</form>'
            );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            barn = form.alder;
            element.scope().$apply();
        }));

        describe('egennaeringCtrl', function () {
            beforeEach(inject(function ($controller, data) {
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
                }
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




        });
        describe('vernepliktCtrl', function () {
            beforeEach(inject(function ($controller, $compile) {
                ctrl = $controller('VernepliktCtrl', {
                    $scope: scope
                });

                $compile(element)(scope);
                scope.$digest();
                form = scope.form;
                element.scope().$apply();

            }));

            it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.valider();
                expect(scope.runValidationBleKalt).toEqual(true);
            });
            it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
                spyOn(scope, "runValidation").andReturn(true);
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('verneplikt');
                expect(scope.settValidert).toHaveBeenCalledWith('verneplikt');
            });
        });
        describe('UtdanningCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;

                 var utdanningNokkelFaktum = {
                     key: 'utdanning.kveld',
                     value: 'true'
                 };

                scope.data.leggTilFaktum(utdanningNokkelFaktum);

                ctrl = $controller('UtdanningCtrl', {
                    $scope: scope
                });
            }));

            it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.valider();
                expect(scope.runValidationBleKalt).toEqual(true);
            });
            it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
                spyOn(scope, "runValidation").andReturn(true);
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('utdanning');
                expect(scope.settValidert).toHaveBeenCalledWith('utdanning');
            });
            it('taben skal vaere apen nar formen ikke er valid', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('utdanning');
            });
            it('ved minst en av checkboksene avhuket skal harHuketAvCheckboks settes til true', function () {
                expect(scope.harHuketAvCheckboks.value).toEqual(true);
            });
            it('hvis faktum utdanning.kveld er huket av sa skal hvis(utdanning.kveld, "true") returnere true', function () {
                expect(scope.hvis('utdanning.kveld', 'true')).toEqual(true);
            });
            it('hvis faktum utdanning.kortvarig ikke er huket av sa skal hvis(utdanning.kortvarig, "true") returnere false', function () {
                expect(scope.hvis('utdanning.kortvarig', 'true')).toEqual(false);
            });
            it('hvis faktum utdanning.kortvarig ikke er huket og har aldri vaert huket av sa skal hvis(utdanning.kortvarig, "true") returnere false', function () {
                expect(scope.hvis('utdanning.kortvarig', 'false')).toEqual(false);
            });
            it('hvis faktum utdanning.kortvarig ikke er huket men har vaert huket av sa skal hvis(utdanning.kortvarig, "true") returnere false', function () {
                var kortvarigFaktum = {
                    key: 'utdanning.kortvarig',
                    value: 'false'
                };

                scope.data.leggTilFaktum(kortvarigFaktum);
                expect(scope.hvis('utdanning.kortvarig', 'false')).toEqual(true);
            });
            it('hvis faktum utdanning.kortvarig ikke er huket men har vaert huket av sa skal hvisIkke(utdanning.kortvarig) returnere true', function () {
                var kortvarigFaktum = {
                    key: 'utdanning.kortvarig',
                    value: 'false'
                };

                scope.data.leggTilFaktum(kortvarigFaktum);
                expect(scope.hvisIkke('utdanning.kortvarig')).toEqual(true);
            });
            it('hvis faktum utdanning.kortvarigflere ikke er huket og har ikke vaert huket av sa skal hvisIkke(utdanning.kortvarigflere) returnere false', function () {
                expect(scope.hvisIkke('utdanning.kortvarigflere')).toEqual(false);
            });
            it('hvis faktum utdanning.kveld er huket sa skal hvisIkke(utdanning.kortvarigflere) returnere false', function () {
                expect(scope.hvisIkke('utdanning.kveld')).toEqual(false);
            });
        });
        describe('ReellarbeidssokerCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var faktumDeltid = {
                    key: 'reellarbeidssoker.villigdeltid.reduserthelse',
                    value: 'true'
                };

                var faktumPendle = {
                    key: 'reellarbeidssoker.villigpendle.reduserthelse',
                    value: 'false'
                };

                scope.data.leggTilFaktum(faktumDeltid);
                scope.data.leggTilFaktum(faktumPendle);


                ctrl = $controller('ReellarbeidssokerCtrl', {
                    $scope: scope
                });
            }));

            it('skal returnere true for person over 59 aar', function () {
                scope.alder = 60;
                expect(scope.erOver59Aar()).toBe(true);
            });

            it('skal returnere false for person som er 59 aar', function () {
                scope.alder = 59;
                expect(scope.erOver59Aar()).toBe(false);
            });

            it('skal returnere true for person under 60 aar', function () {
                scope.alder = 59;
                expect(scope.erUnder60Aar()).toBe(true);
            });

            it('skal returnere false for person over 60 aar', function () {
                scope.alder = 62;
                expect(scope.erUnder60Aar()).toBe(false);
            });
            it('skal returnere true for valgt annet unntak deltid', function () {
                scope.deltidannen = {
                    value: 'true'
                };
                expect(scope.harValgtAnnetUnntakDeltid()).toEqual(true);
            });
            it('skal returnere false for ikke huket av valgtAnnetUnntakDeltid', function () {
                scope.deltidannen = null;
                expect(scope.harValgtAnnetUnntakDeltid()).toEqual(false);
                scope.deltidannen = {};
                expect(scope.harValgtAnnetUnntakDeltid()).toEqual(false);
                scope.deltidannen = undefined;
                expect(scope.harValgtAnnetUnntakDeltid()).toEqual(false);
            });
            it('skal returnere false for huket av har ikke valgtAnnetUnntakDeltid', function () {
                scope.deltidannen = {
                    value: 'false'
                };
                expect(scope.harValgtAnnetUnntakDeltid()).toEqual(false);
            });
            it('skal returnere true for valgt annet unntak pendle', function () {
                scope.pendleannen = {
                    value: 'true'
                };
                expect(scope.harValgtAnnetUnntakPendle()).toEqual(true);
            });
            it('skal returnere false for ikke huket av valgtAnnetUnntakPendle', function () {
                scope.pendleannen = null;
                expect(scope.harValgtAnnetUnntakPendle()).toEqual(false);
                scope.pendleannen = {};
                expect(scope.harValgtAnnetUnntakPendle()).toEqual(false);
                scope.pendleannen = undefined;
                expect(scope.harValgtAnnetUnntakPendle()).toEqual(false);
            });
            it('skal returnere false for huket av har ikke valgtAnnetUnntakPendle', function () {
                scope.pendleannen = {
                    value: 'false'
                };
                expect(scope.harValgtAnnetUnntakPendle()).toEqual(false);
            });
            it('harHuketAvCheckboksDeltid skal vaere true nar deltid reduserthelse er huket av', function () {
                expect(scope.harHuketAvCheckboksDeltid.value).toEqual(true);
            });
            it('harHuketAvCheckboksPendle skal vaere false når deltidcheckbokser ikke er huket av', function () {
                expect(scope.harHuketAvCheckboksPendle.value).toEqual('');
            });
            it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.valider();
                expect(scope.runValidationBleKalt).toEqual(true);
            });
            it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
                spyOn(scope, "runValidation").andReturn(true);
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('reellarbeidssoker');
                expect(scope.settValidert).toHaveBeenCalledWith('reellarbeidssoker');
            });
            it('taben skal vaere apen nar formen ikke er valid', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('reellarbeidssoker');
            });
            it('hvis en deltidaarsaker er huket av og så blir den avhuket, sa skal harHuketAvCheckboksDeltid vaere tom ', function () {
                expect(scope.harHuketAvCheckboksDeltid.value).toBe(true);
                scope.data.fakta[1].value = 'false';
                scope.endreDeltidsAarsaker();
                expect(scope.harHuketAvCheckboksDeltid.value).toBe('');
            });
            it('hvis ingen deltidaarsaker er huket av og så blir en aarsak huket av, sa skal harHuketAvCheckboksDeltid vaere true ', function () {
                scope.data.fakta[1].value = 'false';
                scope.endreDeltidsAarsaker();
                expect(scope.harHuketAvCheckboksDeltid.value).toBe('');
                scope.data.fakta[1].value = 'true';
                scope.endreDeltidsAarsaker();
                expect(scope.harHuketAvCheckboksDeltid.value).toBe(true);
            });
            it('hvis en pendleaarsaker er huket av og så blir den avhuket, sa skal harHuketAvCheckboksPendle vaere tom ', function () {
                scope.data.fakta[1].value = 'false';
                scope.endreDeltidsAarsaker();
                expect(scope.harHuketAvCheckboksPendle.value).toBe('');
            });
            it('hvis ingen pendleaarsaker er huket av og så blir en aarsak huket av, sa skal harHuketAvCheckboksPendle vaere true ', function () {
                scope.data.fakta[2].value = 'false';
                scope.endrePendleAarsaker();
                expect(scope.harHuketAvCheckboksPendle.value).toBe('');
                scope.data.fakta[2].value = 'true';
                scope.endrePendleAarsaker();
                expect(scope.harHuketAvCheckboksPendle.value).toBe(true);
            });
            it('krysset av for villigDeltid sa trengerUtalelseFraFagpersonellDeltid vaere true', function () {
                expect(scope.trengerUtalelseFraFagpersonellDeltid()).toBe(true);
            });
            it('ikke krysset av for villigPendle sa trengerUtalelseFraFagpersonellDeltid vaere false', function () {
                expect(scope.trengerUtalelseFraFagpersonellPendle()).toBe(false);
            });
            it('ikke krysset av for villighelse kanIkkeTaAlleTyperArbeid vaere false', function () {
                expect(scope.kanIkkeTaAlleTyperArbeid()).toBe(false);
            });
            it('svart false pa villighelse kanIkkeTaAlleTyperArbeid vaere true', function () {
                var faktum = {
                    key: 'reellarbeidssoker.villighelse',
                    value: 'false'
                }
                scope.data.leggTilFaktum(faktum);
                expect(scope.kanIkkeTaAlleTyperArbeid()).toBe(true);
            });
        });
        describe('BarneCtrl', function () {
            beforeEach(inject(function ($injector, $controller, cms, data, $compile) {
                ctrl = $controller('BarneCtrl', {
                    $scope: scope
                });
                element = angular.element(
                    '<form name="form">' +
                        '<input type="text" ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                        '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>' +
                        '</form>'
                );

                $compile(element)(scope);
                $httpBackend = $injector.get('$httpBackend');


                scope.cms = cms;
                $httpBackend.expectGET('/sendsoknad/rest/landtype/' + scope.barn.properties.land).
                    respond({});

                scope.data = data;
                scope.$digest();

            }));

            it('skal returnere 0 aar for barn fodt idag', function () {
                var idag = new Date();
                var year = idag.getFullYear();
                var month = idag.getMonth() + 1;
                var date = idag.getDate();

                scope.barn.properties.fodselsdato = year + "-" + month + "-" + date;
                expect(scope.finnAlder().toString()).toEqual("0");
            });
            it('skal returnere 1 aar for barn fodt samme dag ifjor', function () {
                var idag = new Date();
                var lastyear = idag.getFullYear() - 1;
                var month = idag.getMonth() + 1;
                var date = idag.getDate();

                scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
                expect(scope.finnAlder().toString()).toEqual("1");
            });
            it('skal returnere 0 aar for barn fodt dagen etter idag ifjor', function () {
                var idag = new Date();
                var lastyear = idag.getFullYear() - 1;
                var month = idag.getMonth() + 1;
                var date = idag.getDate() + 1;

                scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
                expect(scope.finnAlder().toString()).toEqual("0");
            });
            it('skal returnere 0 aar for barn fodt måneden etter idag ifjor', function () {
                var idag = new Date();
                var lastyear = idag.getFullYear() - 1;
                var lastmonth = idag.getMonth() + 2;
                var date = idag.getDate();

                scope.barn.properties.fodselsdato = lastyear + "-" + lastmonth + "-" + date;
                expect(scope.finnAlder().toString()).toEqual("0");
            });
            it('skal vise feilmelding hvis barnet er over 18', function () {
                var idag = new Date();
                var overAtten = idag.getFullYear() - 18;
                var denneManeden = idag.getMonth() + 1;
                var dag = idag.getDate() - 1;
                scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;
                barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
                element.scope().$apply();

                expect(scope.underAtten.value).toEqual("");
                expect(scope.skalViseFeilmelding).toEqual(true);
            });
            it('skal ikke vise feilmelding hvis barnet er under 18', function () {
                var idag = new Date();
                var overAtten = idag.getFullYear() - 17;
                var denneManeden = idag.getMonth() + 1;
                var dag = idag.getDate() - 1;
                scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;
                barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
                element.scope().$apply();

                expect(scope.underAtten.value).toEqual("true");
                expect(scope.skalViseFeilmelding).toEqual(false);
            });
            it('Hvis alder ikke er oppgitt enda så skal ikke feilmeldingen om at barnet må være under 18 vises', function () {
                scope.barn.properties.fodselsdato = "";
                barn.$setViewValue();
                element.scope().$apply();
                expect(scope.skalViseFeilmelding).toEqual(false);
            });
            it('skal returnere true hvis barnetillegg er registrert', function () {
                scope.barn.properties.barnetillegg = 'true';
                expect(scope.barnetilleggErRegistrert()).toBe(true);
            });
            it('skal returnere false hvis barnet ikke har inntekt', function () {
                scope.barn.properties.ikkebarneinntekt = '';
                expect(scope.barnetHarInntekt()).toBe(false);
            });
            it('skal returnere true hvis barnet har inntekt', function () {
                scope.barn.properties.ikkebarneinntekt = 'false';
                expect(scope.barnetHarInntekt()).toBe(true);
            });
            it('skal returnere true hvis barnet ikke har inntekt', function () {
                scope.barn.properties.ikkebarneinntekt = 'true';
                expect(scope.barnetHarIkkeInntekt()).toBe(true);
            });
            it('alder og sammensatt navn skal kun settes hvis formen er valid', function () {
                var idag = new Date();
                var lastyear = idag.getFullYear() - 1;
                var month = idag.getMonth() + 1;
                var date = idag.getDate();

                scope.barn.properties.fodselsdato = lastyear + "-" + month + "-" + date;
                barn.$setViewValue(idag + "-" + month + "-" + date);
                element.scope().$apply();

                scope.barn.properties.fornavn = "Fornavn";
                scope.barn.properties.etternavn = "Etternavn";

                scope.lagreBarn(scope.form);
                expect(scope.barn.properties.alder).toEqual(1);
                expect(scope.barn.properties.sammensattnavn).toEqual("Fornavn Etternavn");
            });
            it('alder og sammensatt navn skal ikke være satt naar formen er valid', function () {
                var idag = new Date();
                var overAtten = idag.getFullYear() - 18;
                var denneManeden = idag.getMonth() + 1;
                var dag = idag.getDate() - 1;
                scope.barn.properties.fodselsdato = overAtten + "-" + denneManeden + "-" + dag;

                barn.$setViewValue(overAtten + "-" + denneManeden + "-" + dag);
                element.scope().$apply();

                scope.barn.properties.fornavn = "Fornavn";
                scope.barn.properties.etternavn = "Etternavn";
                scope.lagreBarn(scope.form);

                expect(scope.barn.properties.alder).toEqual(undefined);
                expect(scope.barn.properties.sammensattnavn).toEqual(undefined);
            });
            it('skal returnere true for EOSland som ikke er norge', function () {
                scope.eosLandType = "eos";
                expect(scope.erEosLandAnnetEnnNorge()).toBe(true);
            });
            it('erEosLandAnnetEnnNorge skal returnere false for norge', function () {
                scope.eosLandType = "Norge";
                expect(scope.erEosLandAnnetEnnNorge()).toBe(false);
                scope.eosLandType = "norge";
                expect(scope.erEosLandAnnetEnnNorge()).toBe(false);
            });
            it('erIkkeEosLand skal returnere false for et land som ikke er i eos', function () {
                scope.eosLandType = "ikkeEos";
                expect(scope.erIkkeEosLand()).toBe(true);
            });
        });
        describe('BarneCtrlMedUrlEndreBarn', function () {
            var cookieStore, location, barnFaktum;
            beforeEach(inject(function (_$httpBackend_, $controller, cms, $cookieStore, $location, data) {
                location = $location;
                location.$$url = 'endrebarn/111';
                scope.data = data;
                scope.data.land = 'NOR';

                barnFaktum = {
                    key: 'barn',
                    faktumId: 111
                };
                scope.data.leggTilFaktum(barnFaktum);

                ctrl = $controller('BarneCtrl', {
                    $scope: scope
                });

                scope.cms = cms;
                cookieStore = $cookieStore;
            }));

            it('hvis et barn blir endret så skal scope.barn inneholde verdiene til barnet som blir endret', function () {
                expect(scope.barn.faktumId).toEqual(111);
            });
        });
        describe('BarneCtrlMedUrlBarnetillegg', function () {
            var cookieStore, location, barnFaktum;
            beforeEach(inject(function (_$httpBackend_, $controller, cms, $cookieStore, $location, data) {
                location = $location;
                location.$$url = 'sokbarnetillegg/111';
                scope.data = data;
                scope.data.land = 'NOR';

                barnFaktum = {
                    key: 'barn',
                    faktumId: 111,
                    properties: {
                        sammensattnavn: 'Fornavn Etternavn'
                    }
                };
                scope.data.leggTilFaktum(barnFaktum);

                ctrl = $controller('BarneCtrl', {
                    $scope: scope
                });

                scope.cms = cms;
                cookieStore = $cookieStore;
            }));

            it('hvis det sokes barnetillegg for et barn som finnes i TPS skal scope.barn inneholde de samme verdiene som barnet det sokes om', function () {
                expect(scope.barn.properties.sammensattnavn).toEqual('Fornavn Etternavn');
            });
            it('soker om barnetilleg for barn i tps og avbryter sa skal ', function () {
                scope.barn.properties.barneinntekttall = 100;
                scope.barn.properties.barnetillegg = true;
                scope.barn.properties.ikkebarneinntekt = true;
                scope.avbrytBarnetilegg(event);
                expect(scope.barn.properties.barneinntekttall).toEqual(undefined);
                expect(scope.barn.properties.barnetillegg).toEqual(undefined);
                expect(scope.barn.properties.ikkebarneinntekt).toEqual(undefined);
            });
        });
        describe('BarnetilleggCtrl', function () {
            var cookieStore;

            beforeEach(inject(function ($controller, data, $cookieStore, $location) {
                location = $location;

                scope.data = data;
                scope.data.land = {
                    result: [
                        {value: 'NOR', text: 'Norge'},
                        { value: 'DNK', text: 'Danmark'}
                    ]};

                var tpsBarn = {
                    key: 'barn',
                    properties: {}
                };
                var nyttBarn = {
                    key: 'barn',
                    properties: {barnetillegg: 'true'},
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(tpsBarn);
                scope.data.leggTilFaktum(nyttBarn);

                cookieStore = $cookieStore

                ctrl = $controller('BarnetilleggCtrl', {
                    $scope: scope
                });
            }));

            it('erBrukerregistrert skal returnere true for brukerregistert barn', function () {
                var barn = {
                    key: 'barn',
                    type: 'BRUKERREGISTRERT'
                };
                expect(scope.erBrukerregistrert(barn)).toEqual(true);
            });
            it('erSystemRegistrert skal returnere true for SYSTEMREGISTRERT barn', function () {
                var barn = {
                    key: 'barn',
                    type: 'SYSTEMREGISTRERT'
                };
                expect(scope.erSystemRegistrert(barn)).toEqual(true);
            });
            it('ingenLandRegistrert skal returnere true ingen land registrert', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: ''}
                };
                expect(scope.ingenLandRegistrert(barn)).toEqual(true);
            });
            it('ingenLandRegistrert skal returnere false med land registrert', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: 'Norge'}
                };
                expect(scope.ingenLandRegistrert(barn)).toEqual(false);
            });
            it('norskBarnIkkeFunnetITPS skal returnere true for brukerregistrert barn og med land NOR', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: 'NOR'},
                    type: 'BRUKERREGISTRERT'
                };
                expect(scope.norskBarnIkkeFunnetITPS(barn)).toEqual(true);
            });
            it('norskBarnIkkeFunnetITPS skal returnere false for brukerregistrert barn og med land DNK', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: 'DNK'},
                    type: 'BRUKERREGISTRERT'
                };
                expect(scope.norskBarnIkkeFunnetITPS(barn)).toEqual(false);
            });
            it('norskBarnIkkeFunnetITPS skal returnere false for systemregistrert barn og med land NOR', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: 'NOR'},
                    type: 'SYSTEMREGISTRERT'
                };
                expect(scope.norskBarnIkkeFunnetITPS(barn)).toEqual(false);
            });
            it('norskBarnIkkeFunnetITPS skal returnere false hvis ikke barn er registrert ', function () {
                var barn = {
                    key: 'barn'
                };
                expect(scope.norskBarnIkkeFunnetITPS(barn)).toEqual(false);
            });
            it('hvis barnet har inntekt så kreves vedlegg', function () {
                var barn = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'false',
                        barnetillegg: 'true'}
                };
                expect(scope.kreverVedlegg(barn)).toEqual(true);
            });
            it('hvis barnet ikke har inntekt så kreves ikke vedlegg', function () {
                var barn = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'true'}
                };
                expect(scope.kreverVedlegg(barn)).toEqual(false);
            });
            it('hvis barnet er norsk og ikke funnet i tps så kreves vedlegg', function () {
                var barn = {
                    key: 'barn',
                    properties: {land: 'NOR'},
                    type: 'BRUKERREGISTRERT'
                };
                expect(scope.kreverVedlegg(barn)).toEqual(true);
            });
            it('erGutt skal returnere true for barn med hannkjønn og false for barn med hunnkjønn', function () {
                var gutt = {
                    key: 'barn',
                    properties: {kjonn: 'm'}
                };
                var jente = {
                    key: 'barn',
                    properties: {kjonn: 'f'}
                };
                expect(scope.erGutt(gutt)).toEqual(true);
                expect(scope.erGutt(jente)).toEqual(false);
            });
            it('erJente skal returnere true for barn med hunnkjønn og false for barn med hannkjønn ', function () {
                var gutt = {
                    key: 'barn',
                    properties: {kjonn: 'm'}
                };
                var jente = {
                    key: 'barn',
                    properties: {kjonn: 'k'}
                };
                expect(scope.erJente(gutt)).toEqual(false);
                expect(scope.erJente(jente)).toEqual(true);
            });
            it('barnetHarInntekt skal returnere true hvis barnet har inntekt og false hvis barnet ikke har inntekt', function () {
                var barnInntekt = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'false'}
                };
                var barnIkkeInntekt = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'true'}
                };
                expect(scope.barnetHarInntekt(barnInntekt)).toBe(true);
                expect(scope.barnetHarInntekt(barnIkkeInntekt)).toBe(false);
            });
            it('barnetHarIkkeInntekt skal returnere true hvis barnet ikke har inntekt og false hvis barnet  har inntekt', function () {
                var barnInntekt = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'false'}
                };
                var barnIkkeInntekt = {
                    key: 'barn',
                    properties: {ikkebarneinntekt: 'true'}
                };
                expect(scope.barnetHarIkkeInntekt(barnInntekt)).toBe(false);
                expect(scope.barnetHarIkkeInntekt(barnIkkeInntekt)).toBe(true);
            });
            it('barnetilleggErRegistrert skal returnere true hvis barnet har barnetillegg', function () {
                var barnIkkeTillegg = {
                    key: 'barn',
                    properties: {barnetillegg: 'false'}
                };
                var barnTillegg = {
                    key: 'barn',
                    properties: {barnetillegg: 'true'}
                };
                expect(scope.barnetilleggErRegistrert(barnIkkeTillegg)).toEqual(false);
                expect(scope.barnetilleggErRegistrert(barnTillegg)).toEqual(true);
            });
            it('barnetilleggIkkeRegistrert skal returnere true hvis barnet ikke har barnetillegg', function () {
                var barnIkkeTillegg = {
                    key: 'barn',
                    properties: {barnetillegg: 'false'}
                };
                var barnTillegg = {
                    key: 'barn',
                    properties: {barnetillegg: 'true'}
                };
                expect(scope.barnetilleggIkkeRegistrert(barnIkkeTillegg)).toEqual(true);
                expect(scope.barnetilleggIkkeRegistrert(barnTillegg)).toEqual(false);
            });
            it('barnetillegg skal vaere false hvis barnet fantes fra for i tps', function () {
                expect(scope.barn[0].properties.barnetillegg).toBe('false');
            });
            it('barnetillegg skal vaere true hvis barnet blir lagt til manuelt', function () {
                expect(scope.barn[1].properties.barnetillegg).toBe('true');
            });
            it('cookien skal bli satt naar et barn blir lagt til', function () {
                scope.leggTilBarn(event);
                expect(cookieStore.get('scrollTil').gjeldendeTab).toBe("#barnetillegg");
            });
            it('cookien skal bli satt naar et barn blir endret til', function () {
                scope.endreBarn(0, event);
                expect(cookieStore.get('scrollTil').gjeldendeTab).toBe("#barnetillegg");
            });
            it('cookien skal bli satt naar et barn soker om barnetillegg', function () {
                scope.sokbarnetillegg(0, event);
                expect(cookieStore.get('scrollTil').gjeldendeTab).toBe("#barnetillegg");
            });
            it('pathen skal endres til nyttbarn naar et barn blir lagt til', function () {
                spyOn(location, 'path');
                scope.leggTilBarn(event);
                expect(location.path).toHaveBeenCalledWith("nyttbarn/");
            });
            it('pathen skal endres til endrebarn/faktumid naar et barn blir endret', function () {
                spyOn(location, 'path');
                scope.endreBarn(0, event);
                expect(location.path).toHaveBeenCalledWith("endrebarn/0");
            });
            it('pathen skal endres til sokbarnetillegg/faktumid naar et barn soker om barnetillegg', function () {
                spyOn(location, 'path');
                scope.sokbarnetillegg(0, event);
                expect(location.path).toHaveBeenCalledWith("sokbarnetillegg/0");
            });
            it('nar et barn slettes skal dette barnet ikke lenger vaere lagret pa fakta', function () {
                expect(scope.data.finnFakta('barn').length).toBe(2);
                scope.slettBarn(scope.barn[0], 0, event);
                expect(scope.data.finnFakta('barn').length).toBe(1);
            });
            it('nar et barnetillegg slettes skal prorpertiene knyttet til barnetillegget resettes', function () {
                expect(scope.barn[1].properties.ikkebarneinntekt).toBe('true');
                scope.slettBarnetillegg(scope.barn[1], 1, event);
                expect(scope.barn[1].properties.ikkebarneinntekt).toBe(undefined);
            });
            it('skal kjøre metodene lukkTab og settValidert nar barnetillegg legges til', function () {
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('barnetillegg');
                expect(scope.settValidert).toHaveBeenCalledWith('barnetillegg');
            });
        });
        describe('AdresseCtrl', function () {
            beforeEach(inject(function ($controller) {
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
        describe('ArbeidsforholdCtrl', function () {
            var cookieStore;
            beforeEach(inject(function ($controller, data, $cookieStore) {
                scope.data = data;
                scope.data.land = {
                    result: [
                        {value: 'NOR', text: 'Norge'},
                        { value: 'DNK', text: 'Danmark'}
                    ]};

                var af1 = {
                    key: 'arbeidsforhold',
                    properties: {
                        type: 'sluttaarsak1',
                        datofra: '11.11.1111'
                    }
                };
                var af2 = {
                    key: 'arbeidsforhold',
                    properties: {
                        type: 'sluttaarsak2',
                        datofra: '11.11.1110'
                    }
                };
                var af3 = {
                    key: 'arbeidsforhold',
                    properties: {
                        type: 'sluttaarsak2',
                        datofra: '11.11.1112'
                    }
                };

                scope.data.leggTilFaktum(af1);
                scope.data.leggTilFaktum(af2);
                scope.data.leggTilFaktum(af3);

                ctrl = $controller('ArbeidsforholdCtrl', {
                    $scope: scope
                });

                cookieStore = $cookieStore;

            }));
            it('arbeidsliste skal inneholde to arbeidsfohold nar to arbeidsforhold er lagt til', function () {
                expect(scope.arbeidsliste.length).toBe(3);
            });
            it('arbeidslisten skal vaere sortert etter arbeidsforholdenes sluttaarsakdato', function () {
                expect(scope.arbeidsliste[0].sluttaarsak.properties.datofra).toBe('11.11.1110');
                expect(scope.arbeidsliste[1].sluttaarsak.properties.datofra).toBe('11.11.1111');
                expect(scope.arbeidsliste[2].sluttaarsak.properties.datofra).toBe('11.11.1112');
            });
            it('arbeidslisten skal inneholder to sluttaarsaker etter at et arbeidsforhold er slettet', function () {

                scope.slettArbeidsforhold(scope.arbeidsliste[0], 0, event);
                expect(scope.arbeidsliste.length).toBe(2);
            });
            it('arbeidslisten skal vare tom etter at alle arbeidsforhold er slettet', function () {
                var event = $.Event("click");
                scope.slettArbeidsforhold(scope.arbeidsliste[0], 0, event);
                scope.slettArbeidsforhold(scope.arbeidsliste[0], 0, event);
                scope.slettArbeidsforhold(scope.arbeidsliste[0], 0, event);
                expect(scope.arbeidsliste.length).toBe(0);
            });
            it('hvis arbeidsforholdet inneholder feil og arbeidsforholdet er lagret så skal feil vises', function () {
                scope.harKlikketKnapp = true;
                scope.harLagretArbeidsforhold = false;
                expect(scope.skalViseFeil()).toEqual(true);
            });
            it('hvis bruker har svart har ikke jobbet så skal harSvart returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'harIkkeJobbet'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.harSvart()).toBe(true);
            });
            it('hvis bruker ikke har svart har ikke jobbet så skal harSvart returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: ''
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.harSvart()).toBe(true);
            });
            it('hvis bruker ikke har jobbet så skal hvisHarJobbet returnere false', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'harIkkeJobbet'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                scope.hvisHarJobbetVarierende();
                expect(scope.hvisHarJobbet()).toBe(false);
            });
            it('hvis bruker har jobbet så skal hvisHarJobbet returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: ''
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarJobbet()).toBe(true);
            });
            it('hvis bruker ikke har jobbet så skal hvisHarIkkeJobbet returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'harIkkeJobbet'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarIkkeJobbet()).toBe(true);
            });
            it('hvis bruker har jobbet så skal hvisHarIkkeJobbet returnere false', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'sdf'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarIkkeJobbet()).toBe(false);
            });
            it('hvis bruker har jobbet varierende så skal hvisHarJobbetVarierende returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'varierendeArbeidstid'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarJobbetVarierende()).toBe(true);
            });
            it('hvis bruker ikke har jobbet varierende så skal hvisHarJobbetVarierende returnere false', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: ''
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarJobbetVarierende()).toBe(false);
            });
            it('hvis bruker har jobbet fast så skal hvisHarJobbetFast returnere true', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: 'fastArbeidstid'
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarJobbetFast()).toBe(true);
            });
            it('hvis bruker ikke har jobbet fast så skal hvisHarJobbetFast returnere false', function () {
                var arbeidstilstand = {
                    key: 'arbeidstilstand',
                    value: ''
                };
                scope.data.leggTilFaktum(arbeidstilstand);
                expect(scope.hvisHarJobbetFast()).toBe(false);
            });
            it('skal returnere Norge for landkode NOR', function () {
                expect(scope.finnLandFraLandkode('NOR')).toEqual("Norge");
            });
            it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
                spyOn(scope, "runValidation").andReturn(true);
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('arbeidsforhold');
                expect(scope.settValidert).toHaveBeenCalledWith('arbeidsforhold');
            });
            it('skal apne bolken for invalid form', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('arbeidsforhold');
            });
            it('skal apne bolken for invalid form', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('arbeidsforhold');
            });
            it('cookieStore skal bli satt når et arbeidsforhold endres', function () {
                scope.endreArbeidsforhold(scope.arbeidsliste[0], 0, event);
                expect(cookieStore.get('scrollTil').gjeldendeTab).toBe("#arbeidsforhold");
            });
            it('cookieStore skal bli satt når et nytt arbeidsforhold legges til', function () {
                scope.nyttArbeidsforhold(event);
                expect(cookieStore.get('scrollTil').gjeldendeTab).toBe("#arbeidsforhold");
            });
        });
        describe('AvbrytCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                ctrl = $controller('AvbrytCtrl', {
                    $scope: scope
                });
                scope.data = data;

            }));
            it('fremdriftsindikatoren skal vises nar man sletter soknaden', function () {
                scope.submitForm();
                expect(scope.fremdriftsindikator.laster).toEqual(true);
            });
        });
        describe('AvbrytCtrlMedBrukerregistrertFakta', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;

                var brukerregistrertFaktum = {
                    key: 'brukerregistrertFaktum',
                    type: 'BRUKERREGISTRERT'
                };
                var brukerregistrertFaktum2 = {
                    key: 'brukerregistrertFaktum',
                    type: 'BRUKERREGISTRERT'
                };

                scope.data.leggTilFaktum(brukerregistrertFaktum);
                scope.data.leggTilFaktum(brukerregistrertFaktum2);

                ctrl = $controller('AvbrytCtrl', {
                    $scope: scope
                });
            }));

            it('skal kreve brekftelse med fakta som er brukerregistrerte', function () {
                expect(scope.krevBekreftelse).toEqual(true);
            });
        });
        describe('DagpengerCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                ctrl = $controller('DagpengerCtrl', {
                    $scope: scope
                });
                scope.data = data;
            }));

            it('bolk skal få valideringsmetode når leggTilValideringsmetode blir kalt', function () {
                expect(scope.grupper[0].valideringsmetode).toBe(undefined);
                scope.leggTilValideringsmetode('reellarbeidssoker', function () {
                });
                expect(scope.grupper[0].valideringsmetode).toNotBe(undefined);
            });
            it('bolk skal bli validert når settValidert blir kalt', function () {
                scope.grupper[0].validering = true;
                expect(scope.grupper[0].validering).toBe(true);
                scope.settValidert('reellarbeidssoker');
                expect(scope.grupper[0].validering).toBe(false);
            });
            it('leggTilStickyFeilmelding skal bli kjort nar stickyFeilmelding blir kalt', function () {
                spyOn(scope, "leggTilStickyFeilmelding");
                scope.stickyFeilmelding();
                expect(scope.leggTilStickyFeilmelding).toHaveBeenCalled();
            });
            it('bolk skal få status apen til true når apneTab blir kalt', function () {
                scope.grupper = [
                    {id: 'bolk', apen: false}
                ];
                scope.apneTab("bolk");
                expect(scope.grupper[0].apen).toBe(true);
            });
            it('bolk skal få status apen til false når lukkTab blir kalt', function () {
                scope.grupper = [
                    {id: 'bolk', apen: true}
                ];
                scope.lukkTab("bolk");
                expect(scope.grupper[0].apen).toBe(false);
            });
            it('bolk1 og bolk2 skal få status apen til false når lukkTab blir kalt for bolk1 og bolk2', function () {
                scope.grupper = [
                    {id: 'bolk1', apen: true},
                    {id: 'bolk2', apen: true}
                ];
                var bolker = ['bolk1', 'bolk2'];
                scope.lukkTab(bolker);
                expect(scope.grupper[0].apen).toBe(false);
                expect(scope.grupper[1].apen).toBe(false);
            });
            it('bolk skal ikke få status apen til false når bolknavnet ikke finnes', function () {
                scope.grupper = [
                    {id: 'bolk', apen: true}
                ];
                scope.lukkTab("bolkFeilNavn");
                expect(scope.grupper[0].apen).toBe(true);
            });
        });
        describe('FeilSideCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                ctrl = $controller('FeilSideCtrl', {
                    $scope: scope
                });
            }));

            it('Mine innsendinger og inngagsporten skal settes til riktig url', function () {
                expect(scope.mineInnsendinger).toEqual("minehenvendelserurl");
                expect(scope.inngangsportenUrl).toEqual("inngangsportenurl");
            });
        });

    });
}());
