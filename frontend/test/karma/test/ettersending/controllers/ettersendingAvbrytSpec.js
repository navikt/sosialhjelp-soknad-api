(function () {
    'use strict';

    describe('Start ettersending-controller', function () {
        var scope, ctrl, httpBackend, timeout;
        var soknadId = 1;
        beforeEach(module('nav.services.ettersending', 'nav.ettersending.controllers.avbryt', 'ngResource'));

        beforeEach(function () {
            window.redirectTilSide = jasmine.createSpy('Redirect spy');
        });

        beforeEach(module(function ($provide) {
            $provide.value("data", {
                soknad: {
                    soknadId: soknadId
                }
            });
        }));

        beforeEach(inject(function ($controller, $rootScope, data) {
            scope = $rootScope;
            scope.data = data;
            ctrl = $controller('EttersendingAvbrytCtrl', {
                $scope: scope
            });
        }));

        beforeEach(inject(function($httpBackend, $timeout) {
            timeout = $timeout;
            httpBackend = $httpBackend;
            $httpBackend
                .expectPOST('/sendsoknad/rest/soknad/delete/' + soknadId)
                .respond();
        }));

        it('skal kunne sende ettersending', function () {
            scope.slettEttersending();
            httpBackend.flush();
            timeout.flush();
            expect(window.redirectTilSide).toHaveBeenCalledWith('/sendsoknad/ettersending/avbrutt');
        });
    });
}());
