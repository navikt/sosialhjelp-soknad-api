(function () {
    'use strict';

    var saksoversiktUrl = "saksoversiktUrl";

    describe('UtslagskritererDagpengerCtrl', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location, epost;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'sendsoknad.services', 'nav.modal'));
        beforeEach(module('nav.utslagskriterierDagpenger', 'nav.feilmeldinger'));

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
            $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
                'hjelpetekst.tekst': 'Hjelpetekst tekst' }
            });
        }));

        beforeEach(inject(function ($controller, $rootScope, data) {
            scope = $rootScope;
            scope.data = data;

            ctrl = $controller('utslagskritererDagpengerCtrl', {
                $scope: scope
            });
            scope.$digest();
        }));

        describe('UtslagskritererDagpengerCtrl', function () {
            it('alle url skal bli statt til riktig url', function () {
                expect(scope.alderspensjonUrl).toEqual('alderspensjonUrl');
                expect(scope.saksoversiktUrl).toEqual(saksoversiktUrl);
                expect(scope.reelArbeidsokerUrl).toEqual('reelArbeidsokerUrl');
                expect(scope.dittnavUrl).toEqual('dittnavUrl');
            });
            it('hentAdresseLinjer skal returnere et tomt array hvis adresse ikke finnes i utslagskriteriene', function () {
                expect(scope.hentAdresseLinjer()).toEqual([]);
            });
            it('tpsSvarer skal returnere true hvis tpsIkkeSvarer returnerer false', function () {
                expect(scope.tpsSvarer()).toEqual(true);
            });
            it('tpsIkkeSvarer skal returnere false hvis utslagskriterer ikke inneholder en error', function () {
                expect(scope.tpsSvarerIkke()).toEqual(false);
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
            it('fortsettlikevel skal kalle preventDefault på eventet', function () {
                spyOn(event, 'preventDefault');
                scope.fortsettLikevel(event);
                expect(event.preventDefault).toHaveBeenCalled();
            });
            it('kravForDagpengerIkkeOppfylt skal returnere true nar kravene ikke er oppfylt og soknaden ikke er ferdigstilt', function () {
                expect(scope.kravForDagpengerIkkeOppfylt()).toEqual(true);
            });
            it('registrertArbeidssoker skal returnere false nar bruker ikke er registert arbeidssoker', function () {
                expect(scope.registrertArbeidssoker()).toEqual(false);
            });
            it('gyldigAlder skal returnere false nar bruker er for gammel', function () {
                expect(scope.gyldigAlder()).toEqual(false);
            });
            it('ikkeGyldigAlder skal returnere true nar bruker er for gammel', function () {
                expect(scope.ikkeGyldigAlder()).toEqual(true);
            });
            it('bosattINorge skal returnere false nar bruker ikke er bosatt i Norge', function () {
                expect(scope.bosattINorge()).toEqual(false);
            });
            it('ikkeBosattINorge skal returnere true nar bruker ikke er bosatt i Norge', function () {
                expect(scope.ikkeBosattINorge()).toEqual(true);
            });
            it('ikkeRegistrertArbeidssoker skal returnere false nar bruker er registrert arbeidssoker', function () {
                expect(scope.ikkeRegistrertArbeidssoker()).toEqual(false);
            });
            it('registrertArbeidssokerUkjent skal returnere true nar bruker ikke har status som arbeidssoker', function () {
                expect(scope.registrertArbeidssokerUkjent()).toEqual(true);
            });
        });

        describe('InformasjonsSideCtrlAndreUtslagskriterier', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;
                location = $location;
                ctrl = $controller('utslagskritererDagpengerCtrl', {
                    $scope: scope
                });

                scope.data.utslagskriterier.registrertAdresse = "Gatenavn 56, Poststed 1234";
                scope.data.utslagskriterier.error = "Det har skjedd en feil";
                scope.data.utslagskriterier.registrertArbeidssøker = 'REGISTRERT';
                scope.data.utslagskriterier.gyldigAlder = 'true';
                scope.data.utslagskriterier.bosattINorge = 'true';
                scope.data.soknad.status = "FERDIG";

                scope.$apply();
            }));

            it('hentAdresseLinjer skal returnere adressen hvis adresse finnes i utslagskriteriene', function () {
                expect(scope.hentAdresseLinjer()).toEqual(["Gatenavn 56", "Poststed 1234"]);
            });
            it('tpsSvarerIkke skal returnere true hvis utslagskriterer inneholder en error', function () {
                expect(scope.tpsSvarerIkke()).toEqual(true);
            });
            it('soknadErStartet skal returnere false hvis erSoknadStartet ikke er true', function () {
                expect(scope.soknadErStartet()).toEqual(false);
            });
            it('soknadErFerdigstilt skal returnere true hvis data.soknad sin status er ferdig', function () {
                expect(scope.soknadErFerdigstilt()).toEqual(true);
            });
            it('soknadErIkkeFerdigstilt skal returnere false hvis data.soknad sin status er ferdig', function () {
                expect(scope.soknadErIkkeFerdigstilt()).toEqual(false);
            });
            it('registrertArbeidssoker skal returnere true nar bruker er registert arbeidssoker', function () {
                expect(scope.registrertArbeidssoker()).toEqual(true);
            });
            it('gyldigAlder skal returnere true nar bruker ikke er for gammel', function () {
                expect(scope.gyldigAlder()).toEqual(true);
            });
            it('ikkeGyldigAlder skal returnere false nar bruker ikke er for gammel', function () {
                expect(scope.ikkeGyldigAlder()).toEqual(false);
            });
            it('bosattINorge skal returnere false nar bruker ikke er bosatt i Norge', function () {
                expect(scope.bosattINorge()).toEqual(true);
            });
            it('ikkeBosattINorge skal returnere false nar bruker  er bosatt i Norge', function () {
                expect(scope.ikkeBosattINorge()).toEqual(false);
            });
            it('ikkeRegistrertArbeidssoker skal returnere false nar bruker er registrert arbeidssoker', function () {
                expect(scope.ikkeRegistrertArbeidssoker()).toEqual(false);
            });
            it('registrertArbeidssokerUkjent skal returnere false nar bruker har status som arbeidssoker', function () {
                expect(scope.registrertArbeidssokerUkjent()).toEqual(false);
            });
        });

        describe('InformasjonsSideCtrlTredjeUtslagskriterier', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;
                location = $location;
                ctrl = $controller('utslagskritererDagpengerCtrl', {
                    $scope: scope
                });

                scope.data.utslagskriterier.registrertAdresse = "Gatenavn 56, Poststed 1234";
                scope.data.utslagskriterier.error = "Det har skjedd en feil";
                scope.data.utslagskriterier.registrertArbeidssøker = 'REGISTRERT';
                scope.data.utslagskriterier.bosattINorge = 'true';
                scope.data.soknad.status = "UFERDIG";

                scope.$apply();
            }));
            it('soknadErFerdigstilt skal returnere false hvis data.soknad sin status ikke er ferdig', function () {
                expect(scope.soknadErFerdigstilt()).toEqual(false);
            });
            it('kravForDagpengerOppfylt skal endre pathen til routing nar utslagskriteriene ikke inntreffer', function () {
                spyOn(location, 'path');
                scope.fortsettLikevel(event);
                expect(location.path).toHaveBeenCalledWith("/routing");
            });
        });
    });
}());