/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('PermitteringsperiodeNyttCtrl', function () {
        var scope, ctrl;

        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger'));
        beforeEach(module(function ($provide) {
            var fakta = [
                {
                    key: "personalia",
                    properties: {}
                }
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
            $provide.value("$routeParams", {});
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data) {
            scope = $rootScope;
            scope.permitteringsperiode = {};
            ctrl = $controller('ReellarbeidssokerCtrl', {
                $scope: scope
            });
        }));

        describe("validering av perioder", function() {
            it("fjkj", function() {
            });
        });
    });
}());
