(function () {
    'use strict';

    describe('TilleggsopplysningerCtrl', function () {
        var scope, ctrl, form, event;
        event = $.Event("click");

        beforeEach(module('ngCookies', 'app.services'));
        beforeEach(module('app.controllers'));

        beforeEach(module(function ($provide) {
            $provide.value("data", {});
            $provide.value("cms", {'tekster': {'barnetillegg.nyttbarn.landDefault': ''}});
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
            scope.leggTilStickyFeilmelding = function () {
            };

            scope.leggTilValideringsmetode = function (string, funksjon) {
            };

            ctrl = $controller('TilleggsopplysningerCtrl', {
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
            expect(scope.lukkTab).toHaveBeenCalledWith('tilleggsopplysninger');
            expect(scope.settValidert).toHaveBeenCalledWith('tilleggsopplysninger');
        });
        it('taben skal vaere apen nar formen ikke er valid', function () {
            spyOn(scope, "runValidation").andReturn(false);
            spyOn(scope, "apneTab");
            scope.valider(false);
            expect(scope.runValidation).toHaveBeenCalledWith(false);
            expect(scope.apneTab).toHaveBeenCalledWith('tilleggsopplysninger');
        });
    });
}());
