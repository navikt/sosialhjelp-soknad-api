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
  //$scope.data =  soknad.data;

  $scope.soknadData = {"soknadId":1,"gosysId":"Dagpenger","brukerBehandlingId":"100000000",
					"fakta":{
						"fornavn":{"soknadId":1,"key":"fornavn","value":"Ola"},
						"mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"Johan"},
						"etternavn":{"soknadId":1,"key":"etternavn","value":"Nordmann"},
						"fnr":{"soknadId":1,"key":"fnr","value":"01015245464"},
						"adresse":{"soknadId":1,"key":"adresse","value":"Waldemar Thranes Gt. 98B"},
						"postnr":{"soknadId":1,"key":"postnr","value":"0175"},
						"poststed":{"soknadId":1,"key":"poststed","value":"Oslo"}
					}
				}

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