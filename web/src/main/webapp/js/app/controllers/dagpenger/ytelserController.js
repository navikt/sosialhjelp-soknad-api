angular.module('nav.ytelser.controller',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        $scope.ytelser = {
            minstEnAvhuket: false
        }

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.validerYtelser = function() {
            $scope.minstEnAvhuket = false;
            for(var i= 0; i<nokler.length; i++) {
                var nokkel = nokler[i];
                if (checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    $scope.minstEnAvhuket = true;
                }
            }
            console.log($scope.minstEnAvhuket);
        };
    }]);