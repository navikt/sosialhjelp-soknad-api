angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', '$location', '$routeParams', 'soknadService', 'personalia', 'oppsummeringService', function ($scope, $location, $routeParams, soknadService, personalia, oppsummeringService) {
        $scope.personalia = personalia;
        $scope.oppsummeringHtml = '';
        $scope.harbekreftet = {value: false};

        $scope.soknadId = $routeParams.soknadId;
        oppsummeringService.get($scope.soknadId).then(function(markup) {
            $scope.oppsummeringHtml = markup;
        });

        $scope.sendSoknad = function () {
            if ($scope.harbekreftet.value) {
                console.log("HEI");
            }

//            soknadService.send({param: $scope.soknadId, action: 'send'});
//            $location.path('kvittering');
        }
    }])
    .filter('formatterFnr', function() {
        return function(fnr) {
            return fnr.substring(0, 6) + " " + fnr.substring(6, fnr.length);
        };
    });