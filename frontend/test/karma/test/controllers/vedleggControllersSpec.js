/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('VedleggControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'app.services'));
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
                land: 'Norge',
                soknad: {
                    soknadId: 1
                },
                config: {"soknad.sluttaarsak.url": "sluttaarsakUrl",
                    "soknad.lonnskravskjema.url": "lonnskravSkjema",
                    "soknad.permitteringsskjema.url": "permiteringUrl",
                    "minehenvendelser.link.url": "minehenvendelserurl",
                    "soknad.inngangsporten.url": "inngangsportenurl",
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

        describe('VedleggCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
            
                ctrl = $controller('VedleggCtrl', {
                    $scope: scope
                });
            }));

             it('skal kalle metode for å validere form', function () {
                expect(scope.runValidationBleKalt).toEqual(false);
                scope.validerVedlegg(form);
                expect(scope.runValidationBleKalt).toEqual(true);
            });

            it('skal sjekke at ekstra vedlegg er feridg', function() {
                scope.nyttAnnetVedlegg();
                var forventning = {
                    skjemaNummer: "N6",
                    navn: undefined
                }
                expect(scope.ekstraVedleggFerdig(forventning)).toBe(false);
                forventning.navn = "Mitt ekstra vedlegg";
                expect(scope.ekstraVedleggFerdig(forventning)).toBe(true);
            });

            it('skal sjekke om vedlegg er ferdig behandlet', function() {
                var forventning = {
                    innsendingsvalg: 'VedleggKreves'
                }
                expect(scope.vedleggFerdigBehandlet(forventning)).toBe(false);

                forventning.innsendingsvalg = 'LastetOpp';
                expect(scope.vedleggFerdigBehandlet(forventning)).toBe(true);
            });
        });

        describe('validervedleggCtrlMedVedlegg', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                scope.forventning = {
                    innsendingsvalg: "VedleggKreves",
                    storrelse: 42,
                    $save: function() {
                    },
                    $remove: function() {
                       return {then: function() {

                       }}
                    }
                };
                scope.forventninger = [scope.forventning];
                scope.validert = {};
                
                ctrl = $controller('validervedleggCtrl', {
                    $scope: scope
                });
            }));

            it('skal vise feil før vedlegg er lastet opp', function () {
                expect(scope.hiddenFelt).toEqual({value: ''});
                expect(scope.skalViseFeil).toEqual({value: true});
            });

            it('skal vise feil når man endrer innsending til vedleggKreves', function () {
                scope.endreInnsendingsvalg(scope.forventning, "VedleggKreves");
                expect(scope.hiddenFelt).toEqual({value: ''});
                expect(scope.skalViseFeil).toEqual({value: true});
            });

            it('skal ikke vise feil etter å ha valgt sende senere', function() {
                scope.endreInnsendingsvalg(scope.forventning, "SendesSenere");
                expect(scope.hiddenFelt.value).toBe(true);
                expect(scope.skalViseFeil.value).toBe(false);
            });

            it('skal kunne slette annet vedlegg', function() {
                scope.forventning.skjemaNummer = "N6";
                scope.forventning.innsendingsvalg = "LastetOpp";
                scope.slettVedlegg(scope.forventning);

                expect(scope.hiddenFelt).toEqual({value: '' });
                expect(scope.skalViseFeil).toEqual({ value: true });
                expect(scope.validert.value).toEqual(false);
            });

            it('skal kunne slette vanlig vedlegg', function() {
                scope.forventning.innsendingsvalg = "LastetOpp";
                scope.slettVedlegg(scope.forventning);

                expect(scope.hiddenFelt).toEqual({value: '' });
                expect(scope.skalViseFeil).toEqual({ value: true });
                expect(scope.validert.value).toEqual(false);
            });
        });

        describe('validervedleggCtrlUtenVedlegg', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                scope.forventning = {};
            
                scope.forventning.innsendingsvalg = undefined;

                ctrl = $controller('validervedleggCtrl', {
                    $scope: scope
                });
            }));

             it('skal ikke vise feil', function () {
                expect(scope.hiddenFelt).toEqual({value: 'true'});
                expect(scope.skalViseFeil).toEqual({value: false});
            });
        });
    });
}());
