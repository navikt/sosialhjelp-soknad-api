'use strict';

/** jasmine spec */

describe('GrunnlagsdataController', function () {

    var $scope;
    var $controller;
    var $httpBackend;

    beforeEach(module('app.grunnlagsdata'));

    beforeEach(inject(function (_$httpBackend_, $injector) {
        $scope = $injector.get('$rootScope');


        $controller = $injector.get('$controller');

        $httpBackend = _$httpBackend_;
//        $httpBackend.expectGET('/sendsoknad/rest/utslagskriterier/1').
//            respond({"alder":true, "borIUtland":true });

    }));
});

describe('DagpengerControllere', function () {
    var scope, ctrl, form, element;

    beforeEach(module('ngCookies', 'app.services'));
    beforeEach(module('app.controllers', 'nav.feilmeldinger'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {
            fakta: [],
            finnFaktum: function(faktumKey) {},
            finnFakta: function(faktumKey) {},
            soknad: {soknadId:1}
        });
        $provide.value("cms", {});
        $provide.value("personalia", {
            alder: 61
        });
        $provide.constant('lagreSoknadData', "OPPDATER_OG_LAGRE");
    }));

    beforeEach(inject(function ($rootScope, $controller, $compile, $httpBackend) {
        $httpBackend.expectGET('../js/app/directives/feilmeldinger/feilmeldingerTemplate.html').
            respond('');

        scope = $rootScope;
        scope.validateFormFunctionBleKalt = false;
        scope.validateForm = function (form) {
            scope.validateFormFunctionBleKalt = true;
        };
        scope.runValidation = function (form) {
            //expected call..
        };

        element = angular.element(
            '<form name="form">'
                + '<div form-errors></div>'
                + '</form>'
        );

        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        element.scope().$apply();


    }));

    describe('egennaeringCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('EgennaeringCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerOgSettModusOppsummering(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });

        it('skal generere aarstallene fra i år og 4 år bakover', function () {
            //ctrl.genererAarstallListe;
            expect(scope.aarstall.length).toEqual(5);
        })

        it('prevalgte aret skal være fjorårets år', function() {
            var idag = new Date();
            var ifjor = idag.getFullYear();
            expect(scope.forrigeAar).toEqual((ifjor-1).toString())
        })
    });

    describe('vernepliktCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('VernepliktCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerOgSettModusOppsummering(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    });

    describe('UtdanningCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('UtdanningCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerOgSettModusOppsummering(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    });

    describe('ReellarbeidssokerCtrl', function () {
        beforeEach(inject(function ($controller) {
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
    });

    describe('BarneCtrl', function () {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('BarneCtrl', {
                $scope: scope
            });
        }));

        it('skal returnere 0 aar for barn fodt idag', function () {
            var idag = new Date();
            var year = idag.getFullYear();
            var month = idag.getMonth() + 1;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = year + "." + month +"." + date;
            expect(scope.finnAlder().toString()).toEqual("0");
        });

        it('skal returnere 1 aar for barn fodt samme dag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var month = idag.getMonth() + 1;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = lastyear + "." + month +"." + date;
            expect(scope.finnAlder().toString()).toEqual("1");
        });

        it('skal returnere 0 aar for barn fodt dagen etter idag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var month = idag.getMonth() + 1;
            var date = idag.getDate()  + 1;

            scope.barn.properties.fodselsdato = lastyear + "." + month +"." + date;


            expect(scope.finnAlder().toString()).toEqual("0");
        });
        it('skal returnere 0 aar for barn fodt måneden etter idag ifjor', function () {
            var idag = new Date();
            var lastyear = idag.getFullYear() - 1;
            var lastmonth = idag.getMonth() + 2;
            var date = idag.getDate();

            scope.barn.properties.fodselsdato = lastyear + "." + lastmonth +"." + date;
            expect(scope.finnAlder().toString()).toEqual("0");
        });
    });

});
