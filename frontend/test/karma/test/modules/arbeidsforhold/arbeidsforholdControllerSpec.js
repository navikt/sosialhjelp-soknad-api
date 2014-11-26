/** jasmine spec */

/* global $ */

(function () {
    'use strict'

    describe('ArbeidsforholdCtrl', function () {
        var cookieStore;
        var scope, ctrl, form, element, $httpBackend, event, location;
        event = $.Event("click");


        beforeEach(module('ngCookies', 'sendsoknad.services', 'nav.modal'));
        beforeEach(module('nav.arbeidsforhold', 'nav.feilmeldinger'));

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
                finnFaktumMedId: function (faktumId) {
                    var res = null;
                    fakta.forEach(function (item) {
                        if (item.faktumId === faktumId) {
                            res = item;
                        }
                    });
                    return res;
                },
                land: 'Norge',
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
            $provide.value("cms", {tekster: [{'arbeidsforhold.arbeidsiver.landDefault': "Norge"}]});
        }));

        beforeEach(inject(function ($controller, $rootScope, data, $cookieStore) {
            scope = $rootScope;
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

            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };
            scope.apneTab = function () {};
            scope.lukkTab = function () {};
            scope.settValidert = function () {};

            cookieStore = $cookieStore;
            scope.$digest();

        }));
        describe('ArbeidsforholdCtrl', function () {
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

                scope.data.land = 'Norge';

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

                scope.$digest();
                form = scope.form;

            }));

            it('scope.land skal bli satt til samme land som ligger lagret på data', function () {
                expect(scope.land).toEqual("Norge");
            });
            it('hvis et arbeidsforhold skal endres så skal scopet inneholde samme verdier som opprinnelig lå på arbeidsforholdet', function () {
                expect(scope.sluttaarsakType).toEqual('Permittert');
            });
        });
        describe('ArbeidsforholdNyttCtrl', function () {
            beforeEach(inject(function ($injector, $rootScope, $controller, $compile, data, $location) {
                scope = $rootScope;
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

                scope.$digest();
                form = {$name: "formname", $valid: false};

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
    });
}());