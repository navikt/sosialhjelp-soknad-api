(function () {
    'use strict';

    describe('InformasjonsSideCtrl', function () {
        var scope, ctrl, location, httpBackend;

        beforeEach(module('sendsoknad.services', 'nav.services.soknad'));
        beforeEach(module('sendsoknad.controllers'));

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
                leggTilFaktum: function (faktum) {
                    fakta.push(faktum);
                },
                soknad: {
                    soknadId: 1
                },
                config: {"soknad.sluttaarsak.url": "sluttaarsakUrl",
                    "dittnav.link.url": "dittnavUrl",
                    "soknad.lonnskravskjema.url": "lonnskravSkjemaUrl",
                    "soknad.permitteringsskjema.url": "permiteringUrl",
                    "soknad.skjemaveileder.url": "skjemaVeilederUrl",
                    "soknad.brukerprofil.url": "brukerprofilUrl",
                    "soknad.reelarbeidsoker.url": "reelArbeidsokerUrl",
                    "soknad.alderspensjon.url": "alderspensjonUrl",
                    "soknad.dagpengerbrosjyre.url": "dagpengerBrosjyreUrl" }
            });
            $provide.value("cms", {});

        }));

        beforeEach(inject(function ($controller, $rootScope, data, $location, $httpBackend) {
            scope = $rootScope;
            location = $location;
            scope.data = data;
            httpBackend = $httpBackend;

            ctrl = $controller('InformasjonsSideCtrl', {
                $scope: scope
            });

            scope.$digest();
        }));

        it('harlestbrosjyre skal være satt til false hvis brosjyren ikke er lest', function () {
            expect(scope.utslagskriterier.harlestbrosjyre).toEqual(false);
        });
        it('soknadErIkkeStartet skal returnere true hvis ikke soknadErStartet', function () {
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
        it('soknadErStartet skal returnere false hvis erSoknadStartet ikke er true', function () {
            expect(scope.soknadErStartet()).toEqual(false);
        });
        it('soknadErFerdigstilt skal returnere true hvis data.soknad sin status er ferdig', function () {
            scope.data.soknad = {
                status: "FERDIG"
            };

            scope.$digest();
            expect(scope.soknadErFerdigstilt()).toEqual(true);
        });
        it('soknadErIkkeFerdigstilt skal returnere false hvis data.soknad sin status er ferdig', function () {
            scope.data.soknad = {
                status: "FERDIG"
            };
            scope.$digest();

            expect(scope.soknadErIkkeFerdigstilt()).toEqual(false);
        });
        it('harLestBrosjyre skal returnere true hvis lest brosjyre', function () {
            scope.utslagskriterier.harlestbrosjyre = true;
            scope.$digest();

            expect(scope.harLestBrosjyre()).toEqual(true);
        });
        it('startSoknadDersomBrosjyreLest skal kalle startSoknad dersom bruker har lest brosjyre', function () {
            spyOn(scope, 'startSoknad');
            scope.utslagskriterier.harlestbrosjyre = true;
            scope.$digest();

            scope.startSoknadDersomBrosjyreLest();
            expect(scope.startSoknad).toHaveBeenCalled();
        });
        it('startSoknad skal sette fremdriftsindikator til true', function () {
            scope.startSoknad();
            expect(scope.fremdriftsindikator.laster).toBe(true);
        });
        it('startSoknad skal legge på brukerbehandlingsid på pathen', function () {
            httpBackend.expectPOST('/sendsoknad/rest/soknad/opprett')
                .respond({brukerbehandlingId: "brukerbehandlingsid"});

            scope.startSoknad();

            httpBackend.flush();

            expect(location.path()).toBe("/brukerbehandlingsid/soknad/");
        });
    });
}());