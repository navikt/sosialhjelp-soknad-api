angular.module('nav.nyearbeidsforhold.directive',[])
    .directive('nyeArbeidsforhold', function() {
        return {
            replace: false,
            scope: true,
            controller: function ($scope, data) {
                $scope.faktum = data.finnFaktum("nyearbeidsforhold.arbeidsidensist");
                $scope.skalViseFritekstfelt = function() {
                    return isNotNullOrUndefined($scope.faktum.value);
                };
                $scope.faktum = data.finnFaktum("nyearbeidsforhold.arbeidsidensist");
                $scope.harJobbetSidenSistMotattDagpenger = function() {
                    return $scope.faktum.value == "false";
                };
            }
        };
    });