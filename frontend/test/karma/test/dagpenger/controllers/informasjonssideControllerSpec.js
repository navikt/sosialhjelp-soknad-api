(function () {
    'use strict';

    describe('InformasjonsSideCtrl', function () {
        var scope, ctrl, form, element, $httpBackend, location;

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
        }));


        beforeEach(inject(function ($controller, data) {
            scope.data = data;

            ctrl = $controller('InformasjonsSideCtrl', {
                $scope: scope
            });

            scope.$apply();
        }));

        it('harlestbrosjyre skal være satt til false hvis pathen ikke inneholder sendsoknad/soknad', function () {
            expect(scope.utslagskriterier.harlestbrosjyre).toEqual(false);
        });
        it('soknadErIkkeStartet skal returnere true hvis soknadErStartet', function () {
            expect(scope.soknadErIkkeStartet()).toEqual(true);
        });

        it('soknadErIkkeFerdigstilt skal returnere true hvis soknadErFerdigstilt ikke er true', function () {
            expect(scope.soknadErIkkeFerdigstilt()).toEqual(true);
        });
        it('soknadErFerdigstilt skal returnere false hvis data.soknad ikke har status', function () {
            expect(scope.soknadErFerdigstilt()).toEqual(false);
        });
        it('startSoknad skal sette fremdriftsindikator til true', function () {
            scope.startSoknad();
            expect(scope.fremdriftsindikator.laster).toEqual(true);
        });
        it('harLestBrosjyre skal returnere false hvis ikke lest brosjyre', function () {
            expect(scope.harLestBrosjyre()).toEqual(false);
        });
        it('startSoknadDersomBrosjyreLest skal ikke kalle startSoknad dersom bruker ikke har lest brosjyre', function () {
            spyOn(scope, 'harLestBrosjyre');
            scope.startSoknadDersomBrosjyreLest();
            expect(scope.harLestBrosjyre).toHaveBeenCalled();
            expect(scope.harLestBrosjyre()).toNotBe(true);
        });
        it('forsettSoknadDersomBrosjyreLest skal ikke endre path til /soknad dersom brosjyre ikke er lest', function () {
            spyOn(scope, 'harLestBrosjyre');
            scope.forsettSoknadDersomBrosjyreLest();
            expect(scope.harLestBrosjyre).toHaveBeenCalled();
            expect(scope.harLestBrosjyre()).toNotBe(true);
        });
        describe('InformasjonsSideCtrlAndreUtslagskriterier', function () {

            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;
                location = $location;
                ctrl = $controller('InformasjonsSideCtrl', {
                    $scope: scope
                });

                scope.data.utslagskriterier.harlestbrosjyre = true;

                scope.$apply();
            }));

            it('soknadErStartet skal returnere false hvis erSoknadStartet ikke er true', function () {
                expect(scope.soknadErStartet()).toEqual(false);
            });
            it('soknadErFerdigstilt skal returnere true hvis data.soknad sin status er ferdig', function () {
                expect(scope.soknadErFerdigstilt()).toEqual(true);
            });
            it('soknadErIkkeFerdigstilt skal returnere false hvis data.soknad sin status er ferdig', function () {
                expect(scope.soknadErIkkeFerdigstilt()).toEqual(false);
            });
            it('harLestBrosjyre skal returnere true hvis lest brosjyre', function () {
                expect(scope.harLestBrosjyre()).toEqual(true);
            });
            it('startSoknadDersomBrosjyreLest skal kalle startSoknad dersom bruker har lest brosjyre', function () {
                spyOn(scope, 'startSoknad');
                scope.startSoknadDersomBrosjyreLest();
                expect(scope.startSoknad).toHaveBeenCalled();
            });
            it('forsettSoknadDersomBrosjyreLest skal endre path til /soknad dersom brosjyre er lest', function () {
                spyOn(location, 'path');
                scope.startSoknad();
                expect(location.path).toHaveBeenCalledWith("/soknad");
            });
        });
    });
});