/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    var saksoversiktUrl = "saksoversiktUrl";

    describe('DagpengerControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location, epost;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'sendsoknad.services', 'nav.modal'));
        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger'));

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
                land: 'Norge',
                soknad: {
                    soknadId: 1
                },
                config: {"soknad.sluttaarsak.url": "sluttaarsakUrl",
                    "dittnav.link.url": "dittnavUrl",
                    "soknad.lonnskravskjema.url": "lonnskravSkjemaUrl",
                    "soknad.permitteringsskjema.url": "permiteringUrl",
                    "saksoversikt.link.url": saksoversiktUrl,
                    "soknad.skjemaveileder.url": "skjemaVeilederUrl",
                    "soknad.brukerprofil.url": "brukerprofilUrl",
                    "soknad.reelarbeidsoker.url": "reelArbeidsokerUrl",
                    "soknad.alderspensjon.url": "alderspensjonUrl",
                    "soknad.dagpengerbrosjyre.url": "dagpengerBrosjyreUrl" },

                slettFaktum: function (faktumData) {
                    fakta.forEach(function (item, index) {
                        if (item.faktumId === faktumData.faktumId) {
                            fakta.splice(index, 1);
                        }
                    });
                },
                utslagskriterier: {
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
            $provide.value("$routeParams", {});
        })
        )
        ;

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.expectGET('../js/common/directives/feilmeldinger/feilmeldingerTemplate.html').
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
                    '<input type="email" data-ng-model="epost.value" name="epost" data-ng-required="true"/>' +
                    '</form>'
            );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            barn = form.alder;
            epost = form.epost;
            element.scope().$apply();
        }));


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

            it('skal returnere 1 aar for barn fodt tre dager før dagen i dag ifjor', function () {
                var idag = new Date();
                var lastyear = idag.getFullYear() - 1;
                var month = idag.getMonth() + 1;
                var date = idag.getDate() - 3;

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

                cookieStore = $cookieStore;

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
        describe('ArbeidsforholdNyttCtrlEndreModus', function () {
            beforeEach(inject(function ($controller, $compile, data, $location) {
                scope.data = data;
                location = $location;
                location.$$url = 'endrearbeidsforhold/111';

                var af1 = {
                    key: 'arbeidsforhold',
                    properties: {
                        type: 'Permittert',
                        datofra: '11.11.1111'
                    },
                    faktumId: 111
                };

                scope.data.leggTilFaktum(af1);
                ctrl = $controller('ArbeidsforholdNyttCtrl', {
                    $scope: scope
                });

                $compile(element)(scope);
                scope.$digest();
                form = scope.form;
                element.scope().$apply();

            }));

            it('scope.land skal bli satt til samme land som ligger lagret på data', function () {
                expect(scope.land).toEqual("Norge");
            });
            it('hvis et arbeidsforhold skal endres så skal scopet inneholde samme verdier som opprinnelig lå på arbeidsforholdet', function () {
                expect(scope.sluttaarsakType).toEqual('Permittert');
            });
        });
        describe('ArbeidsforholdNyttCtrl', function () {
            beforeEach(inject(function ($injector, $controller, $compile, data, $location) {
                scope.data = data;
                location = $location;
                location.$$url = '/111';

                $httpBackend = $injector.get('$httpBackend');
                $httpBackend.expectGET(/\d/).
                    respond('');

                var af1 = {
                    key: 'arbeidsforhold',
                    properties: {
                        type: 'Permittert',
                        datofra: '11.11.1111'
                    },
                    faktumId: 111
                };


                var lonn = {
                    key: 'lonnsOgTrekkOppgave',
                    properties: {
                        type: 'true'
                    },
                    faktumId: 111
                };

                scope.data.leggTilFaktum(af1);
                scope.data.leggTilFaktum(lonn);
                ctrl = $controller('ArbeidsforholdNyttCtrl', {
                    $scope: scope
                });

                $compile(element)(scope);
                scope.$digest();
                form = scope.form;
                element.scope().$apply();

            }));

            it('scopet skal ikke innheholde verdier fra før når et nytt arbeidsforhold legges til', function () {
                expect(scope.arbeidsforhold.properties.arbeidsgivernavn).toEqual(undefined);
                expect(scope.arbeidsforhold.properties.datofra).toEqual(undefined);
                expect(scope.arbeidsforhold.properties.datotil).toEqual(undefined);
                expect(scope.arbeidsforhold.properties.type).toEqual(undefined);
                expect(scope.arbeidsforhold.properties.eosland).toEqual("false");
                expect(scope.sluttaarsak.properties).toNotBe(undefined);
                expect(scope.sluttaarsak.properties.type).toEqual(undefined);

                scope.lagreArbeidsforhold(form);
                expect(form.$valid).toBe(false);

                scope.arbeidsforhold.properties.arbeidsgivernavn = "A";
                scope.arbeidsforhold.properties.datofra = "2014-10-10";
                scope.arbeidsforhold.properties.datotil = "2014-10-10";
                scope.arbeidsforhold.properties.type = "Avskjediget";
                scope.sluttaarsak.properties.type = "Avskjediget";
                scope.arbeidsforhold.properties.land = "NOR";
                scope.arbeidsforhold.properties.eosland = "false";
                scope.arbeidsforhold.properties.avskjedigetGrunn = "***REMOVED***11111111";
                form.$valid = true;

                scope.lagreArbeidsforhold(form);
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

            it('Mine innsendinger og dittnav skal settes til riktig url', function () {
                expect(scope.mineInnsendinger).toEqual(saksoversiktUrl);
                expect(scope.dittnavUrl).toEqual("dittnavUrl");
            });
        });
        describe('YtelserCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var faktumStonadFisker = {
                    key: 'stonadFisker',
                    value: 'true',
                    $save: function () {
                    }
                };
                scope.data.leggTilFaktum(faktumStonadFisker);

                var faktumSykepenger = {
                    key: 'sykepenger',
                    value: 'true',
                    $save: function () {
                    }
                };
                scope.data.leggTilFaktum(faktumSykepenger);

                ctrl = $controller('YtelserCtrl', {
                    $scope: scope
                });
            }));

            it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
                spyOn(scope, "runValidation").andReturn(true);
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('ytelser');
                expect(scope.settValidert).toHaveBeenCalledWith('ytelser');
            });

            it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.valider();
                expect(scope.runValidationBleKalt).toEqual(true);
            });

            it('taben skal vaere apen nar formen ikke er valid', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('ytelser');
            });

            it('skal ha satt property harHuketAv... når man har lagt inn faktum for stonadFisker', function () {
                expect(scope.harHuketAvCheckboksYtelse.value).toBe('true');
            });

            it('hvis det skjer en endring på en av checkboksene men fortsatt er en av dem avhuket ekskludert den siste sa skal harHuketAvChekboks vaere true', function () {
                scope.endreYtelse();
                expect(scope.harHuketAvCheckboksYtelse.value).toEqual('true');
            });

            it('hvis det skjer en endring på checkboksene slik at ingen er huket av lengre så skal harHuketAvChekboks satt til tom string', function () {
                var faktumStonadFisker = {
                    key: 'stonadFisker',
                    value: 'false',
                    $save: function () {
                    }
                };
                scope.data.leggTilFaktum(faktumStonadFisker);
                scope.endreYtelse();
                expect(scope.harHuketAvCheckboksYtelse.value).toEqual('');
            });

            it('hvis den siste checkboksen blir huket av sa skal alle tidligere checkbokser som er huket av bli avhuket', function () {
                var stonadFisker = {
                    key: 'stonadFisker',
                    value: 'true',
                    $save: function () {
                    }
                };

                var offentligTjenestepensjon = {
                    key: 'offentligTjenestepensjon',
                    value: 'true',
                    $save: function () {
                    }

                };
                var ingenYtelse = {
                    key: 'ingenYtelse',
                    value: 'true',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(stonadFisker);
                scope.data.leggTilFaktum(offentligTjenestepensjon);
                scope.endreYtelse();

                expect(scope.harHuketAvCheckboksYtelse.value).toEqual('true');

                scope.data.leggTilFaktum(ingenYtelse);
                scope.endreIngenYtelse();
                expect(scope.harHuketAvCheckboksYtelse.value).toEqual('true');
            });

            it('hvis den siste checkboksen blir avhuket slik at den ikke er huket av sa skal harHuketAvChekboks settes til tom string', function () {
                var ingenYtelse = {
                    key: 'ingenYtelse',
                    value: 'false',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(ingenYtelse);
                scope.endreIngenYtelse();
                expect(scope.harHuketAvCheckboksYtelse.value).toEqual('');
            });

            it('skal ha satt property harHuketAv... når man har lagt inn faktum for sykepenger', function () {
                expect(scope.harHuketAvCheckboksNavYtelse.value).toBe('true');
            });

            it('hvis det skjer en endring på en av NAVcheckboksene men fortsatt er en av dem avhuket ekskludert den siste sa skal harHuketAvChekboks vaere true', function () {
                scope.endreNavYtelse();
                expect(scope.harHuketAvCheckboksNavYtelse.value).toEqual('true');
            });

            it('hvis det skjer en endring på NAVcheckboksene slik at ingen er huket av lengre så skal harHuketAvChekboks satt til tom string', function () {
                var faktumSykepenger = {
                    key: 'sykepenger',
                    value: 'false',
                    $save: function () {
                    }
                };
                scope.data.leggTilFaktum(faktumSykepenger);
                scope.endreNavYtelse();
                expect(scope.harHuketAvCheckboksNavYtelse.value).toEqual('');
            });

            it('hvis den siste NAVcheckboksen blir huket av sa skal alle tidligere checkbokser som er huket av bli avhuket', function () {
                var faktumSykepenger = {
                    key: 'sykepenger',
                    value: 'true',
                    $save: function () {
                    }
                };

                var offentligTjenestepensjon = {
                    key: 'aap',
                    value: 'true',
                    $save: function () {
                    }

                };
                var ingenYtelse = {
                    key: 'ingennavytelser',
                    value: 'true',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(faktumSykepenger);
                scope.data.leggTilFaktum(offentligTjenestepensjon);
                scope.endreNavYtelse();

                expect(scope.harHuketAvCheckboksNavYtelse.value).toEqual('true');

                scope.data.leggTilFaktum(ingenYtelse);
                scope.endreIngenNavYtelse();
                expect(scope.harHuketAvCheckboksNavYtelse.value).toEqual('true');
            });

            it('hvis den siste NAVcheckboksen blir avhuket slik at den ikke er huket av sa skal harHuketAvChekboks settes til tom string', function () {
                var ingenYtelse = {
                    key: 'ingennavytelser',
                    value: 'false',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(ingenYtelse);
                scope.endreIngenNavYtelse();
                expect(scope.harHuketAvCheckboksNavYtelse.value).toEqual('');
            });
        });
    });
}());
