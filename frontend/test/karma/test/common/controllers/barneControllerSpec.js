/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    var saksoversiktUrl = "saksoversiktUrl";

    describe('BarneController og BarneTilleggController', function () {
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
                config: { },

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
        }));

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
                expect(location.path).toHaveBeenCalledWith("undefined/nyttbarn/");
            });
            it('pathen skal endres til endrebarn/faktumid naar et barn blir endret', function () {
                spyOn(location, 'path');
                scope.endreBarn(0, event);
                expect(location.path).toHaveBeenCalledWith("undefined/endrebarn/0");
            });
            it('pathen skal endres til behandlingId/sokbarnetillegg/faktumid naar et barn soker om barnetillegg', function () {
                spyOn(location, 'path');
                scope.sokbarnetillegg(0, event);
                expect(location.path).toHaveBeenCalledWith("undefined/sokbarnetillegg/0");
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
    });
}());
