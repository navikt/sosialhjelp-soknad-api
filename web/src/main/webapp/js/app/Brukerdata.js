angular.module('app.brukerdata', ['app.services'])

.controller('SoknadDataCtrl', function($scope, $routeParams, soknadService, $location) {
	console.log($routeParams.soknadId);
	$scope.soknadData = soknadService.get({id: $routeParams.soknadId});

/*	$scope.hentSoknadData = function(soknadId) {
		$scope.soknadData = soknadService.get({id: soknadId});
	}*/

	$scope.lagre = function(route) {
		var soknadData = $scope.soknadData;
		soknadData.$save({id: soknadData.soknadId});
		$location.path(route);
	}
})

.controller('ValidationCtrl', function($scope, soknad, $location) {
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
})