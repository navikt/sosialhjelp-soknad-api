(function () {
    'use strict';

    describe('DagpengerControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location, epost;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'gjenopptak.services'));
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
                    "saksoversikt.link.url": "saksoversiktUrl",
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
        );

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
        describe('PersonaliaCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var faktum = {
                    key: 'personalia',
                    properties: {
                        alder: "61",
                        kjonn: 'm',
                        statsborgerskap: 'NOR'
                    },
                    faktumId: 111
                };
                scope.data.leggTilFaktum(faktum);
                ctrl = $controller('PersonaliaCtrl', {
                    $scope: scope
                });
            }));

            it('personalia skal inneholde data', function () {
                expect(scope.personalia).toNotBe(undefined);
            });
            it('brukerprofilUrl skal bli satt til riktig url', function () {
                expect(scope.brukerprofilUrl).toBe("brukerprofilUrl");
            });
            it('hvis personen er en mann så skal erMann returnere true', function () {
                expect(scope.erMann()).toEqual(true);
            });
            it('hvis personen er en mann så skal erKvinne returnere false', function () {
                expect(scope.erKvinne()).toEqual(false);
            });
            it('hvis personalia sa skal harHentetPersonalia returnere true', function () {
                expect(scope.harHentetPersonalia()).toEqual(true);
            });
            it('hvis statsborgerskap er Norge sa skal erUtenlandskStatsborger returnere false', function () {
                expect(scope.erUtenlandskStatsborger()).toEqual(false);
            });
            it('skal kjøre metodene lukkTab og settValidert nar valider kjores', function () {
                spyOn(scope, "lukkTab");
                spyOn(scope, "settValidert");
                scope.valider(false);
                expect(scope.lukkTab).toHaveBeenCalledWith('personalia');
                expect(scope.settValidert).toHaveBeenCalledWith('personalia');
            });
        });
        describe('PersonaliaCtrlKvinne', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var faktum = {
                    key: 'personalia',
                    properties: {
                        alder: "61",
                        kjonn: 'k'
                    }
                };
                scope.data.leggTilFaktum(faktum);
                ctrl = $controller('PersonaliaCtrl', {
                    $scope: scope
                });
            }));

            it('hvis personen er en kvinne så skal erMann returnere false', function () {
                expect(scope.erMann()).toEqual(false);
            });
            it('hvis personen er en kvinne så skal erKvinne returnere true', function () {
                expect(scope.erKvinne()).toEqual(true);
            });
        });
        describe('PersonaliaCtrlIkkeKjonn', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                var faktum = {
                    key: 'personalia',
                    properties: {
                        alder: "61"
                    }
                };
                scope.data.leggTilFaktum(faktum);
                ctrl = $controller('PersonaliaCtrl', {
                    $scope: scope
                });
            }));

            it('hvis personen ikke har et kjønn så skal erMann returnere false', function () {
                expect(scope.erMann()).toEqual(false);
            });
            it('hvis personen ikke har et kjønn så skal erKvinne returnere true', function () {
                expect(scope.erKvinne()).toEqual(false);
            });
        });
    });
}());
