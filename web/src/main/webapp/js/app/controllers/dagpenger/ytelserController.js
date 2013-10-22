angular.module('nav.ytelser.controller',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'personalia'};
        $scope.sidedata = {navn: 'ytelser'};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.validerYtelser = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            form.$setValidity('harValgtYtelse', true); // Fjerne feil som kan være satt dersom man prøver å huke av "nei" mens andre checkboxer er avhuket.
            form.$setValidity("minstEnAvhuket", minstEnAvhuket);
            $scope.validateForm(form.$invalid);

        };

        $scope.hukAvIngenYtelser = function() {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);

            var harValgtYtelse = $scope.erCheckboxerAvhuket(ytelserNokler);
            console.log($scope.soknadData.fakta.ingenYtelse.value);
            if (harValgtYtelse) {
                $scope.soknadData.fakta.ingenYtelse.value = false;
            } else {
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: true});
            }
        }

        $scope.erCheckboxerAvhuket = function(checkboxNokler) {
            var minstEnAvhuket = false;
            for(var i= 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            return minstEnAvhuket;
        }
    }]);