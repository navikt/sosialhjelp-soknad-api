angular.module('brukerdata', ['services'])

.controller('SoknadDataCtrl', ['$scope', 'soknadService', '$location', function($scope, soknadService, $location) {
	
	$scope.hentSoknadData = function(soknadId) {
		$scope.soknadData = soknadService.get({id: soknadId});
	}

	$scope.lagre = function(route) {
		var soknadData = $scope.soknadData;
		soknadData.$save({id: soknadData.soknadId});
		$location.path(route);
	}
}])

/*
Eksempel som viser en get ved bruk av $http. Kan bukes om man ønsker bedre kontroll
*/
/*
function SoknadDataCtrl_http($scope, $http) {
	$http({method: 'GET', url: '/sendsoknad/rest/soknad/' + 1}).
		success(function (data, status) {
			$scope.soknadData = data
		}).
		error(function(data, status){
			alert("En feil skjedde");
		});
}*/

.controller('ValidationCtrl', ['$scope', 'soknad', '$location', function($scope, soknad, $location) {
  $scope.data =  soknad.data;

  $scope.feilmeldinger = {
    paakreves: '*',
    feil: 'Ikke gyldig',
    dato: ' Dato må skrives på formen dd.mm.åååå',
    fratil: 'Fra-dato må være før til-dato'
  }

  $scope.saveState = function() {
    soknadService.data = $scope.data;
  }

  /*
  $scope.setRoute = function(route) {
    $scope.saveState();
    $location.path(route);
  }
  */
}])