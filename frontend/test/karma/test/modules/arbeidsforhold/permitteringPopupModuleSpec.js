/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    var scope, ctrl, permitteringer, permittering;

    var form = {};
    var mockPermitteringer = [
        {key: "arbeidsforhold.permitteringsperiode",
            properties: {permiteringsperiodedatofra: "2010-01-01",
                permiteringsperiodedatotil: "2010-12-12",
                permitteringProsent: "1"}},
        {key: "arbeidsforhold.permitteringsperiode",
            properties: {permiteringsperiodedatofra: "2011-01-01",
                permiteringsperiodedatotil: "2011-12-12",
                permitteringProsent: "2"}
        }
    ];

    var endrePermittering = {
        key: "arbeidsforhold.permitteringsperiode",
        properties: {permiteringsperiodedatofra: "1112-11-11",
            permiteringsperiodedatotil: "1112-11-11",
            permitteringProsent: "2"}
    };
    var nyPermittering = endrePermittering;

    beforeEach(module('nav.arbeidsforhold', 'nav.feilmeldinger', 'sendsoknad.services'));
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
        $provide.value("$modal", {});


    }));

    describe('PermitteringsperiodeNyttCtrl', function () {
        beforeEach(module(function ($provide) {
            $provide.value("$modalInstance", {open: function () {
            }, close: function () {
            }});

            $provide.value("permitteringer", mockPermitteringer);

            $provide.value("permittering");
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data, _permitteringer_) {
            scope = $rootScope;
            scope.permitteringsperiode = {};
            permitteringer = _permitteringer_;
            ctrl = $controller('PermitteringPopupCtrl', {
                $scope: scope
            });
        }));

        it("Endremodus skal være satt til false når permittering er tomt", function () {
            expect(scope.endreModus).toBeFalsy();
            expect(scope.cmsPostFix).toBe("");
        });
        it("Orginal permitteringsperiode skal være tom for ny permitteringsperiode", function () {
            expect(scope.originalPermitteringsperiode).toBe(undefined);
        });
        it("permitteringsperiode skal være et nytt faktum", function () {
            expect(scope.permitteringsperiode.key).toBe('arbeidsforhold.permitteringsperiode');
        });
        it("alleAndrePermitteringsperioder skal inneholde alle permitteringer", function () {
            expect(scope.alleAndrePermitteringsperioder).toEqual(permitteringer);
        });
        it("lagreNyPermitteringsPeriode skal legge ny permittering på permitteringer", function () {
            expect(permitteringer.length).toBe(2);
            var nyPermittering = {key: "arbeidsforhold.permitteringsperiode", properties: {permiteringsperiodedatofra: "1112-11-11", permiteringsperiodedatotil: "1112-11-11", permitteringProsent: "2"}};

            scope.lagreNyPermitteringsPeriode(nyPermittering);
            expect(permitteringer.length).toBe(3);
        });
    });
    describe('PermitteringPopupCtrl med endring av permittering', function () {
        beforeEach(module(function ($provide) {
            $provide.value("$modalInstance", {open: function () {
            }, close: function () {
            }});
            $provide.value("permitteringer", mockPermitteringer);

            $provide.value("permittering", endrePermittering);
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data, _permitteringer_, _permittering_) {
            scope = $rootScope;
            scope.permitteringsperiode = {};
            permitteringer = _permitteringer_;
            permittering = _permittering_;
            ctrl = $controller('PermitteringPopupCtrl', {
                $scope: scope
            });
        }));
        it("Endremodus skal være satt til true når permittering er en permittering", function () {
            expect(scope.endreModus).toBeTruthy();
            expect(scope.cmsPostFix).toBe(".endre");
        });
        it("Orginal permitteringsperiode skal være permitteringen som skal endres", function () {
            expect(scope.originalPermitteringsperiode).toBe(permittering);
        });
        it("Permitteringsperiode skal være en kopi av permitteringen som ble sendt inn", function () {
            expect(scope.permitteringsperiode).toEqual(angular.copy(permittering));
            permittering.test = "entest";
            expect(scope.permitteringsperiode).not.toEqual(permittering);
        });
    });
    describe('permitteringsperiodeNyttCtrl ikke i endreModus', function () {
        beforeEach(module(function ($provide) {
            $provide.value("$modalInstance", {open: function () {
            }, close: function () {
            }});
            $provide.value("permitteringer", mockPermitteringer);

            $provide.value("permittering", endrePermittering);
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, data, _permitteringer_, _permittering_) {
            scope = $rootScope;
            scope.permitteringsperiode = {
                properties: {
                    permiteringsperiodedatofra: "2011-05-05",
                    permiteringsperiodedatotil: "2011-06-06"
                }
            };
            scope.alleAndrePermitteringsperioder = permitteringer;


            scope.lagreNyPermitteringsPeriode = function (permitteringsperiode) {
            };
            scope.lukk = function () {
            };
            permitteringer = _permitteringer_;
            permittering = _permittering_;
            ctrl = $controller('permitteringsperiodeNyttCtrl', {
                $scope: scope
            });
        }));
        it("lagrePermitteringsperiode skal broadcaste formnavnet", function () {
            form.$name = "TestForm";
            spyOn(scope, "$broadcast");
            scope.lagrePermitteringsperiode(form);
            expect(scope.$broadcast).toHaveBeenCalledWith("RUN_VALIDATIONTestForm");
        });
        it("lagrePermitteringsperiode skal legge til ny permitteringsperiode i permitteringslisten når formen er valid og ikke i endreModus", function () {
            form.$name = "TestForm";
            form.$valid = true;
            scope.permitteringsperiode = nyPermittering;

            spyOn(scope, "lagreNyPermitteringsPeriode");
            spyOn(scope, "lukk");
            scope.endreModus = false;
            scope.$apply();

            scope.lagrePermitteringsperiode(form);
            expect(scope.lagreNyPermitteringsPeriode).toHaveBeenCalledWith(nyPermittering);
            expect(scope.lukk).toHaveBeenCalled();
        });
        it("lagrePermitteringsperiode skal oppdatereEksisterendePermitteringsPeriode når formen er valid og i endreModus", function () {
            form.$name = "TestForm";
            form.$valid = true;
            scope.permitteringsperiode = nyPermittering;

            spyOn(scope, "lagreNyPermitteringsPeriode");
            spyOn(scope, "lukk");
            scope.endreModus = true;
            scope.$apply();

            scope.lagrePermitteringsperiode(form);
            expect(scope.lagreNyPermitteringsPeriode).not.toHaveBeenCalled();
            expect(scope.lukk).toHaveBeenCalled();
        });
        it("lagrePermitteringsperiode og form ikke valid skal lukk ikke kalles", function () {
            form.$name = "TestForm";
            form.$valid = false;

            spyOn(scope, "lukk");

            scope.lagrePermitteringsperiode(form);
            expect(scope.lukk).not.toHaveBeenCalled();
        });
        it("datoIntervallErValidert skal være falsey om perioden er i en eksisterende periode", function () {
            scope.datoIntervallErValidert.value = "true";
                scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-05-05";
                scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-06-06";

            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeFalsy();
        });

        it("datoIntervallErValidert skal være falsey om perioden delvis overlapper en eksisterende periode", function () {
            scope.datoIntervallErValidert.value = "true";
            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2009-05-05";
            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2010-01-10";
            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeFalsy();
        });

        it("datoIntervallErValidert skal være truthy om perioden er etter alle andre perioder", function () {
            scope.datoIntervallErValidert.value = "true";
            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2013-05-05";
            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2013-01-10";
            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeTruthy();
        });

        it("datoIntervallErValidert skal være truthy om perioden er før alle andre perioder", function () {
            scope.datoIntervallErValidert.value = "true";
            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2001-05-05";
            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2001-01-10";
            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeTruthy();
        });

        it("skal ikke godta at permitteringsperioden starter på samme dag som en en annen periode avslutter (non-inclusive)", function () {
            scope.datoIntervallErValidert.value = "true";
            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2011-12-12";
            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2011-12-20";
            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeFalsy();
        });

        it("skal ikke godta at permitteringsperioden slutter på samme dag som en en annen periode starter (non-inclusive)", function () {
            scope.datoIntervallErValidert.value = "true";
            scope.permitteringsperiode.properties.permiteringsperiodedatofra = "2009-03-12";
            scope.permitteringsperiode.properties.permiteringsperiodedatotil = "2010-01-01";
            scope.$digest();
            expect(scope.datoIntervallErValidert.value).toBeFalsy();
        });
    });
})();
