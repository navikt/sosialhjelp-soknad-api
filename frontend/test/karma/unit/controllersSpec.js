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

    beforeEach(module('app.services', 'app.controllers', 'nav.feilmeldinger'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {alder: {'alder': 61},
            fakta: [], finnFaktum: function(faktumKey) {}});
        $provide.value("cms", {});
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


        scope.soknadData = {
            fakta: {}
        }

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

        it('skal kalle metode for å validere form', function () {
            scope.soknadData.fakta.villigdeltid = true;
            scope.soknadData.fakta.villigpendle = true;
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerOgSettModusOppsummering(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    });
});
