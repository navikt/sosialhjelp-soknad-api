(function () {
    'use strict';

    describe('bekreftelsesController', function () {
        var scope, ctrl, httpBackend, timeoutEn;
        var saksovesiktUrl = "saksoversiktUrl";

        beforeEach(module('nav.bekreftelse', 'nav.services.bekreftelse', 'sendsoknad.services'));

        beforeEach(function () {
            window.redirectTilUrl = jasmine.createSpy('Redirect URL spy');
        });

        beforeEach(module(function ($provide) {
            var fakta = [];

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
                    brukerBehandlingId: 123
                },
                config: {"saksoversikt.link.url": saksovesiktUrl},
                soknadOppsett: {
                    temaKode: 'DAG'
                }
            });

            $provide.value("cms", {});
            $provide.value("$routeParams", {behandlingsId: 123});
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data, $httpBackend, $timeout) {
            scope = $rootScope;
            httpBackend = $httpBackend;
            timeoutEn = $timeout;
            scope.data = data;

            var faktum = {
                key: 'personalia',
                properties: {
                    epost: "min@epost.no"
                }
            };
            scope.data.leggTilFaktum(faktum);

            ctrl = $controller('BekreftelsesCtrl', {
                $scope: scope
            });
        }));

        it('Eposten skal være den samme som ligger på personaliaobjektet hvis ikke annet er satt', function () {
           expect(scope.epost.value).toBe("min@epost.no");
        });
        it('Sendtepost, fullfort og fremdriftsindikater skal alle vaere initielt satt til false', function () {
            expect(scope.sendtEpost.value).toBe(false);
            expect(scope.fullfort.value).toBe(false);
            expect(scope.fremdriftsindikator.laster).toBe(false);
        });
        it('skal redirecte til saksoversikt hvis formen validerer', function () {
            var form = {
                $valid: true
            };

            httpBackend.expectPOST('/sendsoknad/rest/bekreftelse/123')
                .respond({});

            scope.sendEpost(form);
            httpBackend.flush();
            timeoutEn.flush();
            expect(window.redirectTilUrl).toHaveBeenCalledWith(saksovesiktUrl+"/detaljer/" + scope.temaKode.value + "/123");
        });
        it('Hvis epost er oppgitt og formen validerer så skal sendtEpost settes til true', function () {
            var form = {
                $valid: true
            };
            scope.sendEpost(form);
            expect(scope.sendtEpost.value).toBe(true);
        });
        it('Hvis epost ikke er oppgitt og formen validerer så skal sendtEpost fortsatt vaere false', function () {
            scope.epost.value = "";
            var form = {
                $valid: true
            };
            scope.sendEpost(form);
            expect(scope.sendtEpost.value).toBe(false);
        });
        it('Hvis formen validerer så skal fullfort og fremdriftsindikator settes til true', function () {
            var form = {
                $valid: true
            };
            scope.sendEpost(form);
            expect(scope.fullfort.value).toBe(true);
            expect(scope.fremdriftsindikator.laster).toBe(true);
        });
        it('Hvis formen vikke aliderer så skal fullfort og fremdriftsindikator fortsatt vaere false', function () {
            var form = {
                $valid: false
            };
            scope.sendEpost(form);
            expect(scope.sendtEpost.value).toBe(false);
            expect(scope.fullfort.value).toBe(false);
            expect(scope.fremdriftsindikator.laster).toBe(false);
        });
    });
}());
