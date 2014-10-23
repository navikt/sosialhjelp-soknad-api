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

        it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
            spyOn(scope, "runValidation").andReturn(true);
            scope.valider(false);
            expect(scope.runValidation).toHaveBeenCalledWith(false);
        });
    });
}());