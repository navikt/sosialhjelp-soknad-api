/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('OpplastingControllere', function () {
        var scope, ctrl, form, element, barn, $httpBackend, event, location;
        event = $.Event("click");

        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger'));
        beforeEach(module('ngCookies', 'sendsoknad.services'));

        beforeEach(module(function ($provide) {
            var fakta = [
            {}
            ];

            $provide.value("data", {
                soknad: {
                    soknadId: 1
                }
            });
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
            $provide.value("$routeParams", {});
        }));

        beforeEach(inject(function ($injector, $rootScope, $controller, $compile) {
            $httpBackend = $injector.get('$httpBackend');

            $httpBackend.expectGET('../js/common/directives/feilmeldinger/feilmeldingerTemplate.html').
            respond('');

            scope = $rootScope;
            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            element = angular.element(
                '<form name="form">' +
                '<div form-errors></div>' +
                '<input type="text" ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>' +
                '</form>'
                );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            element.scope().$apply();
        }));

        describe('OpplastingVedleggCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;

                ctrl = $controller('OpplastingVedleggCtrl', {
                    $scope: scope
                });
            }));

            it("skal hente vedlegg", function() {
                expect(scope.vedlegg).toNotBe(undefined);
            });
        });

        describe('OpplastingCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                
                ctrl = $controller('OpplastingCtrl', {
                    $scope: scope
                });

                scope.queue = [];
            }));

            it("skal få feil ved tom kø", function() {
                scope.leggVed();
                expect(scope.skalViseFeilmelding).toBe(true);
            });

            it("skal kunne legge ved dokumenter", function() {
                scope.queue.push({name:"mittdokument"});
                scope.leggVed();
                expect(scope.skalViseFeilmelding).toBe(false);
                expect(scope.fremdriftsindikator.laster).toBe(true);
            });
            it("filopplastingstartet skal sette feilmelding til false, og opplastinfeilet til false og fremdrifsindikator til true", function() {
                scope.$broadcast('fileuploadstart');
                expect(scope.fremdriftsindikator.laster).toBe(true);
                expect(scope.skalViseFeilmelding).toBe(false);
                expect(scope.data.opplastingFeilet).toBe(false);
            });
        });

        describe('SlettOpplastingCtrl', function () {
            beforeEach(inject(function ($controller, data) {
                scope.data = data;
                scope.file = {
                    $destroy: function() {

                    },
                    $remove: function() {
                       return {then: function() {
                            scope.fileCleared = true;            
                       }};
                    },
                    name: "a"
                };

                ctrl = $controller('SlettOpplastingCtrl', {
                    $scope: scope
                });
            }));

            it('skal slette opplasting', function() {
                expect(scope.file.name).toEqual("a");
                scope.file.$destroy();
                expect(scope.fileCleared).toBe(true);
            });
        });
    });
}());
