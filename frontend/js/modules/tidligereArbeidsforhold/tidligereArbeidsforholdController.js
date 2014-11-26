angular.module('nav.tidligerearbeidsforhold.controller', [])
    .controller('TidligereArbeidsforholdCtrl', function ($scope) {
        $scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('tidligerearbeidsforhold');
                $scope.settValidert('tidligerearbeidsforhold');
            } else {
                $scope.apneTab('tidligerarbeidsforhold');
            }
        };
    });