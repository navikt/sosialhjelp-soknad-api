/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    var saksoversiktUrl = "saksoversiktUrl";

    describe('GjenopptakController', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location, epost;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'gjenopptak.services', 'nav.modal'));
        beforeEach(module('gjenopptak.controllers', 'nav.feilmeldinger'));

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
        describe('Gjenopptak', function () {
            beforeEach(inject(function ($controller, data) {
                ctrl = $controller('GjenopptakCtrl', {
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
