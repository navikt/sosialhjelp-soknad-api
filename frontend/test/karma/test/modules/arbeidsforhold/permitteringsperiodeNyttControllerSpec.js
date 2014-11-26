/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('PermitteringsperiodeNyttCtrl', function () {
        var scope, ctrl, persister, form;

        beforeEach(module('nav.arbeidsforhold', 'nav.feilmeldinger', 'sendsoknad.services'));
        beforeEach(module(function ($provide) {

            var fakta = [];

//            var endrePermittering = {key: "arbeidsforhold.permitteringsperiode", properties: {permiteringsperiodedatofra: "1112-11-11", permiteringsperiodedatotil: "1112-11-11", permitteringProsent: "2"}};

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

            $provide.value("$modalInstance", {open: function () {
            }, close: function () {
            }});

            $provide.value("permitteringer", [
                {key: "arbeidsforhold.permitteringsperiode",
                    properties: {permiteringsperiodedatofra: "1111-11-11",
                        permiteringsperiodedatotil: "1111-11-11",
                        permitteringProsent: "1"}}
            ]);

            $provide.value("permittering");
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data) {
            scope = $rootScope;
            scope.permitteringsperiode = {};
            scope.runValidation = function () {
            };
            ctrl = $controller('PermitteringPopupCtrl', {
                $scope: scope
            });
        }));


        it("Endremodus skal være satt til false når permittering er tomt", function () {
            expect(scope.endreModus).toBeFalsy();
        });

//        it("skal ikke persistere om form ikke er valid", function () {
////            scope.datoIntervallErValidert.value = "";
//            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
//            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";
//            scope.permitteringsperiode.properties.permitteringProsent = 100;
//            form.$valid = false;
//            scope.lagrePermitteringsperiode(form);
//        });
//
//
//        describe("validering av permitteringsperioden (om den overlapper andre perioder)", function () {
//            it("datoIntervallErValidert skal være falsey om perioden er i en eksisterende periode", function () {
////                scope.datoIntervallErValidert.value = "true";
//                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
//                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";
//                scope.$digest();
////                expect(scope.datoIntervallErValidert.value).toBeFalsy();
//            });
//
//            it("datoIntervallErValidert skal være falsey om perioden delvis overlapper en eksisterende periode", function () {
////                scope.datoIntervallErValidert.value = "true";
//                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2009-05-05";
//                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2010-01-10";
//                scope.$digest();
////                expect(scope.datoIntervallErValidert.value).toBeFalsy();
//            });
//
//            it("datoIntervallErValidert skal være truthy om perioden er etter alle andre perioder", function () {
////                scope.datoIntervallErValidert.value = "true";
//                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2013-05-05";
//                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2013-01-10";
//                scope.$digest();
////                expect(scope.datoIntervallErValidert.value).toBeTruthy();
//            });
//
//            it("datoIntervallErValidert skal være truthy om perioden er før alle andre perioder", function () {
////                scope.datoIntervallErValidert.value = "true";
//                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2001-05-05";
//                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2001-01-10";
//                scope.$digest();
////                expect(scope.datoIntervallErValidert.value).toBeTruthy();
//            });
//
//            it("skal ikke godta at permitteringsperioden starter på samme dag som en en annen periode avslutter (non-inclusive)", function () {
//
////                expect(scope.datoIntervallErValidert.value).toBeFalsy();
//            });
//
//            it("skal ikke godta at permitteringsperioden slutter på samme dag som en en annen periode starter (non-inclusive)", function () {
//
////                expect(scope.datoIntervallErValidert.value).toBeFalsy();
//            });
//        });
    });
}());
