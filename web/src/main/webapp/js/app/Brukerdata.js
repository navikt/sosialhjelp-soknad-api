angular.module('app.brukerdata', ['app.services'])

.controller('SoknadDataCtrl', function($scope, $routeParams, soknadService, $location, $timeout) {
	$scope.soknadData = soknadService.get({id: $routeParams.soknadId});


/*	$scope.hentSoknadData = function(soknadId) {
		$scope.soknadData = soknadService.get({id: soknadId});
	}*/
	function lagre() {
		$timeout(function() {
			var soknadData = $scope.soknadData;
			soknadData.$save({id: soknadData.soknadId});
			lagre();
		}, 60000);
	}

	lagre();

})

.factory('time', function($timeout) {
	var time = {};

	(function tick() {
		//var soknadData = $scope.soknadData;
		//soknadData.$save({id: soknadData.soknadId});
		time.now = new Date().toString();
		$timeout(tick, 1000);
	})();
	return time;
}) 

.controller('SistLagretCtrl', function($scope, time) {
	$scope.time = time;
})

.controller('ValidationCtrl', function($scope, soknad, $location) {
  $scope.data =  soknad.data;

  $scope.feilmeldinger = {
    paakreves: '*',
    feil: 'Ikke gyldig',
    dato: ' Dato må skrives på formen dd.mm.åååå',
    fratil: 'Fra-dato må være før til-dato',
    prosent: 'Må være et tall mellom 0 og 100.'
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