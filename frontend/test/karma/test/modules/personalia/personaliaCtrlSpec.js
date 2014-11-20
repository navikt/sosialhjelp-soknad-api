describe('Personalia controller tests', function () {
    var scope, ctrl;

    beforeEach(module('nav.personalia', 'templates-main'));

    describe('PersonaliaCtrl', function () {
        beforeEach(inject(function ($controller, $rootScope) {
            scope = $rootScope;
            scope.lukkTab = function () {
            };
            scope.settValidert = function () {
            };

            scope.data = {};
            ctrl = $controller('PersonaliaCtrl', {
                $scope: scope
            });
        }));
        it('skal kjøre metodene lukkTab og settValidert nar valider kjores', function () {
            spyOn(scope, "lukkTab");
            spyOn(scope, "settValidert");
            scope.valider(false);
            expect(scope.lukkTab).toHaveBeenCalledWith('personalia');
            expect(scope.settValidert).toHaveBeenCalledWith('personalia');
        });
    });
});


