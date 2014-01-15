angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', '$location', '$routeParams', 'soknadService', 'personalia', function ($scope, $location, $routeParams, soknadService, personalia) {
        $scope.personalia = personalia;
        console.log($scope.personalia);

        $scope.sendSoknad = function () {
            soknadService.send({param: $routeParams.soknadId, action: 'send'});
            $location.path('kvittering');
        }
    }])
    .filter('formatterFnr', function() {
        return function(fnr) {
            return fnr.substring(0, 6) + " " + fnr.substring(6, fnr.length);
        };
    });