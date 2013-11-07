angular.module('nav.forsettsenere',[])
    .controller('FortsettSenereCtrl', ['$scope', 'soknadService', '$routeParams', '$http', function ($scope, soknadService, $routeParams, $http) {
        $scope.soknadData;

		soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;

            $scope.soknadData.fakta.personalia = {};
            $scope.soknadData.fakta.personalia.epost = { 
            	key: "epost",
				soknadId: $scope.soknadData.soknadId,
				type: "SYSTEM",
				value: "ketil.s.velle@nav.no"
			};
        });

        $scope.forsettSenere = function() {
			console.log("fortsett senere klikk");
			$http.post('/sendsoknad/rest/soknad/' + $routeParams.soknadId +'/fortsettsenere', $scope.soknadData.fakta.personalia.epost.value)
				.success(function(data) {
					console.log("naa er jeg happy");
				});
        	console.log("sendt fortsett senere");

        }

    }]);


        