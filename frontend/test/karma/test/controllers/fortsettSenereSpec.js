(function () {
    'use strict';

    describe('DagpengerControllere', function () {
        var scope, ctrl, form, element, $httpBackend, event, location, epost;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'sendsoknad.services'));
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
                soknad: {
                    soknadId: 1
                },
                config: {
                    "dittnav.link.url": "dittnavUrl"}

            });
            $provide.value("cms");
            $provide.value("$routeParams", {});
        })
        );

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
            scope = $rootScope;

            element = angular.element(
                '<form name="form">' +
                    '<input type="email" data-ng-model="epost.value" name="epost" data-ng-required="true"/>' +
                    '</form>'
            );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            epost = form.epost;
            element.scope().$apply();
        }));

        describe('FortsettSenereCtrl', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;

                var epostFaktum = {
                    key: 'epost',
                    value: 'epost@epost.no'
                };
                scope.data.leggTilFaktum(epostFaktum);

                location = $location;
                ctrl = $controller('FortsettSenereCtrl', {
                    $scope: scope
                });

                scope.$apply();
            }));

            it('scope.epost skal bli satt til eposten som ligger på data', function () {
                expect(scope.epost.value).toEqual('epost@epost.no');
            });
            it('scope.forrigeSide skal bli satt til /soknad hvis den ikke finnes fra før', function () {
                expect(scope.forrigeSide).toEqual('/soknad');
            });
            it('sette riktig urler', function () {
                expect(scope.dittnavUrl).toEqual('dittnavUrl');
            });

        });
        describe('FortsettSenereCtrlUtenEpost', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;
                scope.forrigeSide = "Forrige side";

                scope.data = data;
                var faktum = {
                    key: 'personalia',
                    properties: {
                        alder: "61"
                    }
                };
                scope.data.leggTilFaktum(faktum);

                location = $location;
                ctrl = $controller('FortsettSenereCtrl', {
                    $scope: scope
                });

                scope.$apply();
            }));

            it('scope.epost.value skal bli satt til undefined hvis ikke epost finnes fra før ', function () {
                expect(scope.epost.value).toEqual(undefined);
            });
            it('scope.forrigeSide skal bli satt til /soknad hvis den ikke finnes fra før', function () {
                expect(scope.forrigeSide).toEqual('Forrige side');
            });
            it('skal få tilsendt kvittering til eposten som er blitt oppgitt hvis form er valid', function () {
                expect(scope.epost.value).toEqual(undefined);
                scope.epost.value = "min@epost.no";
                var validForm = {
                    key: 'form',
                    $valid: true
                };
                scope.fortsettSenere(validForm);
                expect(scope.epost.value).toBe("min@epost.no");
            });
            it('skal ikke få tilsendt kvittering til eposten som er blitt oppgitt hvis form er invalid', function () {
                expect(scope.epost.value).toEqual(undefined);
                var validForm = {
                    key: 'form',
                    $valid: false
                };

                scope.fortsettSenere(validForm);
                expect(scope.epost.value).toBe(undefined);
            });
            it('hvis eposten endres så er det denne kvitteringen skal sendes til hvis formen er valid', function () {
                expect(scope.epost.value).toEqual(undefined);
                var validForm = {
                    key: 'form',
                    $valid: true
                };

                epost.$setViewValue("min@epost.no");
                element.scope().$apply();

                scope.fortsettSenere(validForm);
                expect(scope.epost.value).toBe("min@epost.no");
            });
            it('hvis bruekr har epost fra før og den endres så er det denne kvitteringen skal sendes til hvis formen er valid', function () {
                scope.epost.value = "min@epost.no";
                expect(scope.epost.value).toEqual("min@epost.no");
                var validForm = {
                    key: 'form',
                    $valid: true
                };
                epost.$setViewValue("minAndre@epost.no");
                element.scope().$apply();

                scope.fortsettSenere(validForm);
                expect(scope.epost.value).toBe("minAndre@epost.no");
            });
        });
        describe('FortsettSenereKvitteringCtrl', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;

                var epostFaktum = {
                    key: 'epost',
                    value: 'epost@epost.no'
                };
                scope.data.leggTilFaktum(epostFaktum);

                location = $location;
                ctrl = $controller('FortsettSenereKvitteringCtrl', {
                    $scope: scope
                });

                scope.$apply();
            }));

            it('scope.epost skal bli satt til eposten som ligger på data', function () {
                expect(scope.epost.value).toEqual('epost@epost.no');
            });
            it('scope.forrigeSide skal bli satt til /soknad hvis den ikke finnes fra før', function () {
                expect(scope.forrigeSide).toEqual('/soknad');
            });
            it('sette riktig urler', function () {
                expect(scope.dittnavUrl).toEqual('dittnavUrl');
            });
        });
        describe('FortsettSenereKvitteringCtrlMedForrigeSide', function () {
            beforeEach(inject(function ($controller, data, $location) {
                scope.data = data;
                scope.forrigeSide = "Forrige side";

                var epostFaktum = {
                    key: 'epost',
                    value: 'epost@epost.no'
                };
                scope.data.leggTilFaktum(epostFaktum);

                location = $location;
                ctrl = $controller('FortsettSenereKvitteringCtrl', {
                    $scope: scope
                });

                scope.$apply();
            }));

            it('scope.forrigeSide skal bli satt til /soknad hvis den ikke finnes fra før', function () {
                expect(scope.forrigeSide).toEqual('Forrige side');
            });
        });
    });
}());
