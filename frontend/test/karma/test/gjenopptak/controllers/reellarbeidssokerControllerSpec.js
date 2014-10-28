(function () {
    'use strict';

    describe('ReellarbeidssokerCtrl', function () {
        var scope, ctrl, form, event;
        event = $.Event("click");

        beforeEach(module('gjenopptak.services'));
        beforeEach(module('gjenopptak.controllers'));

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
                soknad: {
                    soknadId: 1
                },
                slettFaktum: function (faktumData) {
                    fakta.forEach(function (item, index) {
                        if (item.faktumId === faktumData.faktumId) {
                            fakta.splice(index, 1);
                        }
                    });
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
        })
        );

        beforeEach(inject(function ($injector, $rootScope, $controller, data) {
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

            scope.data = data;
            var faktumDeltid = {
                key: 'reellarbeidssoker.villigdeltid.reduserthelse',
                value: 'true'
            };

            var faktumPendle = {
                key: 'reellarbeidssoker.villigpendle.reduserthelse',
                value: 'false'
            };

            var faktum = {
                key: 'personalia',
                properties: {
                    alder: "61"
                }
            };
            scope.data.leggTilFaktum(faktum);
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
            scope.data.fakta[2].value = 'false';
            scope.endreDeltidsAarsaker();
            expect(scope.harHuketAvCheckboksDeltid.value).toBe('');
        });
        it('hvis ingen deltidaarsaker er huket av og så blir en aarsak huket av, sa skal harHuketAvCheckboksDeltid vaere true ', function () {
            scope.data.fakta[2].value = 'false';
            scope.endreDeltidsAarsaker();
            expect(scope.harHuketAvCheckboksDeltid.value).toBe('');
            scope.data.fakta[2].value = 'true';
            scope.endreDeltidsAarsaker();
            expect(scope.harHuketAvCheckboksDeltid.value).toBe(true);
        });
        it('hvis en pendleaarsaker er huket av og så blir den avhuket, sa skal harHuketAvCheckboksPendle vaere tom ', function () {
            scope.data.fakta[1].value = 'false';
            scope.endreDeltidsAarsaker();
            expect(scope.harHuketAvCheckboksPendle.value).toBe('');
        });
        it('hvis ingen pendleaarsaker er huket av og så blir en aarsak huket av, sa skal harHuketAvCheckboksPendle vaere true ', function () {
            scope.data.fakta[3].value = 'false';
            scope.endrePendleAarsaker();
            expect(scope.harHuketAvCheckboksPendle.value).toBe('');
            scope.data.fakta[3].value = 'true';
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
            };
            scope.data.leggTilFaktum(faktum);
            expect(scope.kanIkkeTaAlleTyperArbeid()).toBe(true);
        });
    });
}());
