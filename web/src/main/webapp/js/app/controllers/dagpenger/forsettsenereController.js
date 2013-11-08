angular.module('nav.forsettsenere',[])
    .controller('FortsettSenereCtrl', ['$scope', 'soknadService', '$routeParams', '$http', '$location', 
        function ($scope, soknadService, $routeParams, $http,  $location) {
        $scope.soknadData;

		soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;

            	/*
            $scope.soknadData.fakta.personalia = {};
            $scope.soknadData.fakta.personalia.epost = { 
            	key: "epost",
				soknadId: $scope.soknadData.soknadId,
				type: "SYSTEM",
				value: "ketil.s.velle@nav.no"
			};*/

			console.log($scope.soknadData);
        });

        $scope.forsettSenere = function() {
            var soknadId = $routeParams.soknadId;
			$http.post('/sendsoknad/rest/soknad/' + soknadId +'/fortsettsenere', $scope.soknadData.fakta.personalia.epost.value)
				.success(function(data) {
					console.log("mail sendt til " + $scope.soknadData.fakta.personalia.epost.value);
                    $location.path('kvittering-fortsettsenere/' + soknadId);
				});
        	console.log("sendt fortsett senere");
        }

        $scope.soknadErUnderArbeid = function() {
        	return $scope.soknadData.status == 'UNDER_ARBEID';
        }

        $scope.soknadErFerdig = function() {
            var status =  $scope.soknadData.status;
            console.log("status " + status);
            if (status == 'FERDIG') {
                return true;
            } else {
                return false;
            }
        }
        
}]);


        