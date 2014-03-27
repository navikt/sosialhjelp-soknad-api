(function () {
    'use strict';

    describe('vernepliktCtrl', function () {
        var scope, ctrl, element;
        beforeEach(module('ngCookies', 'sendsoknad.services'));
        beforeEach(module('sendsoknad.controllers', 'nav.feilmeldinger'));

        beforeEach(module(function ($provide) {
            $provide.value("data", {});
            $provide.value("cms", {});
        })
        )
        ;

        beforeEach(inject(function ($injector, $rootScope, $controller) {
            scope = $rootScope;
            scope.runValidationBleKalt = false;
            scope.runValidation = function () {
                scope.runValidationBleKalt = true;
            };

            scope.apneTab = function () {
            };
            scope.lukkTab = function () {
            };
            scope.settValidert = function () {
            };

            ctrl = $controller('VernepliktCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function () {
            expect(scope.runValidationBleKalt).toEqual(false);
            scope.valider();
            expect(scope.runValidationBleKalt).toEqual(true);
        });
        it('skal kjøre metodene lukkTab og settValidert for valid form', function () {
            spyOn(scope, "runValidation").andReturn(true);
            spyOn(scope, "lukkTab");
            spyOn(scope, "settValidert");
            scope.valider(false);
            expect(scope.runValidation).toHaveBeenCalledWith(false);
            expect(scope.lukkTab).toHaveBeenCalledWith('verneplikt');
            expect(scope.settValidert).toHaveBeenCalledWith('verneplikt');
        });
    });
}());
