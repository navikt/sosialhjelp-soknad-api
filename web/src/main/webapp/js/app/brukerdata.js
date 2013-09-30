angular.module('app.brukerdata', ['app.services'])

.controller('StartSoknadCtrl', function($scope, $location, soknadService) {
	$scope.startSoknad = function() {
		var soknadType = window.location.pathname.split("/")[3];
		$scope.soknad = soknadService.create({param: soknadType}).$promise.then(function(result) {
            $location.path('reell-arbeidssoker/' + result.id);
		});
	}
})

.controller('HentSoknadDataCtrl', function($scope, $routeParams, soknadService){
    var soknadId = $routeParams.soknadId;
    $scope.soknadData = soknadService.get({param:  soknadId});
})

.controller('SoknadDataCtrl', function($scope, soknadService, $location, $timeout) {

	console.log('SoknadId: '+  $scope.soknad.id);
	console.log("HENT SOKNAD");
	$scope.soknadData = soknadService.get({param:  $scope.soknad.id});

	$scope.lagre = function() {

		var soknadData = $scope.soknadData;
		console.log("lagre: " + soknadData);
		soknadData.$save({id: soknadData.soknadId});
	}
	
	/*
	function lagre() {
		$timeout(function() {
			var soknadData = $scope.soknadData;
			soknadData.$save({id: soknadData.soknadId});
			lagre();
		}, 60000);
	}
	lagre();
	*/

})

.directive('modFaktum', function() {
	return function( $scope, element, attrs ) {
		element.bind('blur', function() {
			$scope.soknadData.fakta[attrs.name] = {"soknadId":$scope.soknadData.soknadId,"key":attrs.name,"value":element.val()}; 
			$scope.$apply(); 
			$scope.lagre();
		});
	};
})

.factory('time', function($timeout) {
	var time = {};

	(function tick() {
		time.now = new Date().toString();
		$timeout(tick, 1000);
	})();
	return time;
}) 

.controller('SistLagretCtrl', function($scope, time) {
	$scope.time = time;
})

