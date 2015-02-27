(function () {
    'use strict';

    describe('UtdanningCtrl', function () {
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
        )
        ;

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
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
        }));
        describe('UtdanningCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;

                var utdanningNokkelFaktum = {
                    key: 'utdanning.kveld',
                    value: 'true',
                    $save: function () {
                    }
                };
                var utdanning = {
                    key: 'utdanning',
                    value: 'underUtdanning'
                };

                scope.data.leggTilFaktum(utdanningNokkelFaktum);
                scope.data.leggTilFaktum(utdanning);

                ctrl = $controller('UtdanningCtrl', {
                    $scope: scope
                });
            }));

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
                expect(scope.lukkTab).toHaveBeenCalledWith('utdanning');
                expect(scope.settValidert).toHaveBeenCalledWith('utdanning');
            });
            it('taben skal vaere apen nar formen ikke er valid', function () {
                spyOn(scope, "runValidation").andReturn(false);
                spyOn(scope, "apneTab");
                scope.valider(false);
                expect(scope.runValidation).toHaveBeenCalledWith(false);
                expect(scope.apneTab).toHaveBeenCalledWith('utdanning');
            });
            it('ved minst en av checkboksene avhuket skal harHuketAvCheckboks settes til true', function () {
                expect(scope.harHuketAvCheckboks.value).toEqual(true);
            });
            it('hvis underUtdanning sa skal hvis(utdanning, "underUtdanning") returnere true', function () {
                expect(scope.hvis('utdanning', "underUtdanning")).toEqual(true);
            });
            it('hvis ikke svart underUtdanning sa skal hvis(utdanning, "underUtdanning") returnere false', function () {
                var utdanning = {
                    key: 'utdanning',
                    value: ''
                };

                scope.data.leggTilFaktum(utdanning);
                expect(scope.hvis('utdanning', "underUtdanning")).toEqual(false);
            });
            it('', function () {
                expect(scope.hvis('utdanning.kortvarig', 'false')).toEqual(false);
            });
            it('hvis checkbokspm underUtdanningAnnet ikke er svart sa skal hvis(underUtdanning) returnere false', function () {
                expect(scope.hvis('underUtdanningAnnet')).toEqual(false);
            });
            it('hvis checkbokspm underUtdanningAnnet er svart sa skal hvis(underUtdanning) returnere true', function () {
                var underUtdanningAnnet = {
                    key: 'underUtdanningAnnet',
                    value: 'true'
                };

                scope.data.leggTilFaktum(underUtdanningAnnet);
                expect(scope.hvis('underUtdanningAnnet')).toEqual(true);
            });
            it('hvis det skjer en endring på en av checkboksene men fortsatt er en av dem avhuket ekskludert den siste sa skal harHuketAvChekboks vaere true', function () {
                scope.endreUtdanning();
                expect(scope.harHuketAvCheckboks.value).toEqual(true);
            });
            it('hvis det skjer en endring på checkboksene slik at ingen er huket av lengre så skal harHuketAvChekboks satt til tom string', function () {
                var utdanningNokkelFaktum = {
                    key: 'utdanning.kveld',
                    value: 'false'
                };
                scope.data.leggTilFaktum(utdanningNokkelFaktum);

                scope.endreUtdanning();
                expect(scope.harHuketAvCheckboks.value).toEqual('');
            });
            it('hvis den siste checkboksen blir huket av sa skal alle tidligere checkbokser som er huket av bli avhuket', function () {
                var utdanningNokkelFaktum = {
                    key: 'utdanning.kveld',
                    value: 'true',
                    $save: function () {
                    }
                };

                var utdanningkortvarigFaktum = {
                    key: 'utdanning.kortvarig',
                    value: 'true',
                    $save: function () {
                    }

                };
                var underUtdanningAnnet = {
                    key: 'underUtdanningAnnet',
                    value: 'true',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(utdanningNokkelFaktum);
                scope.data.leggTilFaktum(utdanningkortvarigFaktum);
                scope.data.leggTilFaktum(underUtdanningAnnet);

                scope.endreUtdannelseAnnet();
                expect(scope.harHuketAvCheckboks.value).toEqual('true');
            });
            it('hvis den siste checkboksen blir avhuket slik at den ikke er huket av sa skal harHuketAvChekboks settes til tom string', function () {
                var underUtdanningAnnet = {
                    key: 'underUtdanningAnnet',
                    value: 'false',
                    $save: function () {
                    }
                };

                scope.data.leggTilFaktum(underUtdanningAnnet);
                scope.endreUtdannelseAnnet();
                expect(scope.harHuketAvCheckboks.value).toEqual('');
            });
        });
        describe('UtdanningCtrlUtenCheckbokserHuketAv', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;

                ctrl = $controller('UtdanningCtrl', {
                    $scope: scope
                });
            }));

            it('ved ingen av checkboksene avhuket skal harHuketAvCheckboks settes til tom string', function () {
                expect(scope.harHuketAvCheckboks.value).toEqual('');
            });
        });
    });
}());
