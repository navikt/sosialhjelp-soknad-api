(function () {
    'use strict';

    describe('routingForGjenopptakController', function () {
        var scope, ctrl;

        beforeEach(module('utslagskriterierDagpenger.controllers'));

        beforeEach(inject(function ($injector, $rootScope, $controller) {
            scope = $rootScope;
            scope.runValidationBleKalt = false;

            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            ctrl = $controller('routingForGjenopptakCtrl', {
                $scope: scope
            });
        }));
    });
}());