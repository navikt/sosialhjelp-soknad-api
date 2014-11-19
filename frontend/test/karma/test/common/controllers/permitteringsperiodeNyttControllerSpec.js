/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('PermitteringsperiodeNyttCtrl', function () {
        var scope, ctrl, persister, form;

        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger', 'sendsoknad.services'));
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

            form = {$valid: true, $name: ""};
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, Faktum, data, datapersister) {
            persister = datapersister;
            datapersister.set("arbeidsforholdData", {});

            var periode1 = {properties: {permiteringsperiodedatofra: "2010-01-01", permiteringsperiodedatotil: "2010-12-12"}};
            var periode2 = {properties: {permiteringsperiodedatofra: "2011-01-01", permiteringsperiodedatotil: "2011-12-12"}};
            datapersister.set("allePermitteringsperioder", [periode1, periode2]);

            scope = $rootScope;
            scope.permitteringsperiode = {};
            scope.runValidation = function() {};
            ctrl = $controller('PermitteringsperiodeNyttCtrl', {
                $scope: scope
            });
        }));

        describe("permitteringperiodeNyttController persistering", function() {
            it("faktumet, med properties, skal pushes til barnefaktum på datapersisteren når det lagres", function() {
                expect(persister.get("barnefaktum")).toBeFalsy();
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";
                scope.permitteringsperiode.properties.permitteringProsent=100;
                scope.lagrePermitteringsperiode(form);
                expect(persister.get("barnefaktum").length).toBe(1);
            });

            it("skal ikke persistere om form ikke er valid", function() {
                expect(persister.get("barnefaktum")).toBeFalsy();
                scope.datoIntervallErValidert.value = "";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";
                scope.permitteringsperiode.properties.permitteringProsent=100;
                form.$valid = false;
                scope.lagrePermitteringsperiode(form);
                expect(persister.get("barnefaktum")).toBeFalsy();
            });
        });

        describe("validering av permitteringsperioden (om den overlapper andre perioder)", function() {
            it("datoIntervallErValidert skal være falsey om perioden er i en eksisterende periode", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeFalsy();
            });

            it("datoIntervallErValidert skal være falsey om perioden delvis overlapper en eksisterende periode", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2009-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2010-01-10";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeFalsy();
            });

            it("datoIntervallErValidert skal være truthy om perioden er etter alle andre perioder", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2013-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2013-01-10";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeTruthy();
            });

            it("datoIntervallErValidert skal være truthy om perioden er før alle andre perioder", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2001-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2001-01-10";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeTruthy();
            });

            it("skal ikke godta at permitteringsperioden starter på samme dag som en en annen periode avslutter (non-inclusive)", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-12-12";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-12-20";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeFalsy();
            });

            it("skal ikke godta at permitteringsperioden slutter på samme dag som en en annen periode starter (non-inclusive)", function() {
                scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2009-03-12";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2010-01-01";
                scope.$digest();
                expect(scope.datoIntervallErValidert.value).toBeFalsy();
            });
        });
    });
}());
