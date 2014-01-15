angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', '$location', '$routeParams', 'soknadService', 'personalia', 'oppsummeringService', function ($scope, $location, $routeParams, soknadService, personalia, oppsummeringService) {
        $scope.personalia = personalia;
        $scope.oppsummeringHtml = '';
        oppsummeringService.get($routeParams.soknadId).then(function(markup) {
            $scope.oppsummeringHtml = markup;
        });

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