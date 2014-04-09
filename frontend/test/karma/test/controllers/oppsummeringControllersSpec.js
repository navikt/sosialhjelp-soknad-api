/** jasmine spec */

/* global $ */

(function () {
    'use strict';

    describe('OppsummeringControllere', function () {
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
            $httpBackend = g.get('$httpBackend');

            $httpBackend.expectGET('../js/common/directives/feilmeldinger/feilmeldingerTemplate.html').
                respond('');

            scope = $rootScope;
            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            scope.fnr = "22068511111";
            element = angular.element(
                '<div>'+
                '<form name="form">' +
                    '<div form-errors></div>' +
                    '<input type="text" ng-model="scope.barn.properties.fodselsdato" name="alder"/>' +
                    '<input type="hidden" data-ng-model="underAtten.value" data-ng-required="true"/>' +
                    '</form>' +
                '<span> {{fnr | formatterFnr}} </span>' +
                '</div>'
            );

            $compile(element)(scope);
            scope.$digest();
            form = scope.form;
            element.scope().$apply();
        }));

        describe('OppsummeringCtrl', function () {
            beforeEach(inject(function ($controller, data, $injector,$compile) {
                scope.data = data;
            
                ctrl = $controller('OppsummeringCtrl', {
                    $scope: scope
                });

                $httpBackend = $injector.get('$httpBackend');
            }));

            it('skal kunne sende soknad etter å ha krysset av checkbox', function() {
                expect(scope.harbekreftet).toEqual({value: ''});
                expect(scope.skalViseFeilmelding.value).toEqual(false);
                
                scope.sendSoknad();
                expect(scope.skalViseFeilmelding.value).toEqual(true);

                $httpBackend.expectGET(/\d/).
                respond('');

                scope.harbekreftet.value = true;
                scope.$digest();
                scope.sendSoknad();
                expect(scope.skalViseFeilmelding.value).toEqual(false);                
            });

            it('skal formattere fnr på riktig måte', function() {
                expect(element.find("span").text()).toEqual(" 220685 11111 ");
            });
        });
    });
}());
