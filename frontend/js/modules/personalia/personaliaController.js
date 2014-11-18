angular.module('nav.personalia.controller', [])
    .controller('PersonaliaCtrl', function ($scope) {
        $scope.valider = function () {
            scope.lukkTab('personalia');
            scope.settValidert('personalia');
        };
    });