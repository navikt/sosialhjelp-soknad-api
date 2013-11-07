angular.module('nav.forsettsenere',[])
    .controller('FortsettSenereCtrl', ['$scope', 'soknadService', '$routeParams', function ($scope, soknadService, $routeParams) {
        $scope.soknadData;

		soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;

            $scope.soknadData.fakta.personalia = {};
            $scope.soknadData.fakta.personalia.epost = { 
            	key: "epost",
				soknadId: $scope.soknadData.soknadId,
				type: "SYSTEM",
				value: "mock@epost.com"
			};
        });

    }]);


        