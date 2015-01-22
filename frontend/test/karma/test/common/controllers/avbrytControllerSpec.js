/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    var saksoversiktUrl = "saksoversiktUrl";

    describe('AvbrytModulen', function () {
        var scope, element;

        beforeEach(module('ngCookies', 'sendsoknad.services', 'nav.modal', 'nav.avbryt'));
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
                config: { },

                slettFaktum: function (faktumData) {
                    fakta.forEach(function (item, index) {
                        if (item.faktumId === faktumData.faktumId) {
                            fakta.splice(index, 1);
                        }
                    });
                }
            });
            $provide.value("cms", {'tekster': {}});
            $provide.value("$routeParams", {});
        }));

        beforeEach(inject(function ($rootScope, $compile, $templateCache, $httpBackend, data) {
            $templateCache.put('../js/modules/avbryt/templates/slettSoknadTemplate.html', '<span>noe innhold</span>');
            $httpBackend.when("POST", "/sendsoknad/rest/soknad/delete/1").respond();
            $rootScope.data = data;

            var elem = angular.element("<div data-slett-soknad></div>");
            var brukerregistrertFaktum = {
                key: 'brukerregistrertFaktum',
                type: 'BRUKERREGISTRERT'
            };
            var brukerregistrertFaktum2 = {
                key: 'brukerregistrertFaktum',
                type: 'BRUKERREGISTRERT'
            };

            $rootScope.data.leggTilFaktum(brukerregistrertFaktum);
            $rootScope.data.leggTilFaktum(brukerregistrertFaktum2);

            element = $compile(elem)($rootScope);
            scope = element.scope();
            scope.$digest();
        }));

        describe('SlettSoknadDirective', function () {
            beforeEach(inject(function (data) {
                scope.data = data;
            }));
            it('fremdriftsindikatoren skal vises nar man sletter soknaden', function () {
                scope.submitForm();
                expect(scope.fremdriftsindikator.laster).toEqual(true);
            });
        });

        describe('SlettSoknadDirective', function () {
            it('skal kreve brekftelse med fakta som er brukerregistrerte', function () {
                expect(scope.krevBekreftelse).toEqual(true);
            });
        });
    });
}());
