angular.module('nav.routingForGjenopptak', [])
    .controller('routingForGjenopptakCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.gjenopptak = {
            harMotattDagpenger: null
        };
        $scope.dittnavUrl = data.config["dittnav.link.url"];
    }]);