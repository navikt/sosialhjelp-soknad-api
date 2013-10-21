angular.module('nav.ytelser.controller',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.validerYtelser = function(form) {
            // Fjerne feil som kan være satt dersom man prøver å huke av "nei" mens andre checkboxer er avhuket.
            form.$setValidity('harValgtYtelse', true);

            var minstEnAvhuket = false;
            for(var i= 0; i<nokler.length; i++) {
                var nokkel = nokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            form.$setValidity("minstEnAvhuket", minstEnAvhuket);
        };
    }]);