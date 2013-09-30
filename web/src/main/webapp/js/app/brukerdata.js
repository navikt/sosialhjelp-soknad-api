angular.module('app.brukerdata', ['app.services'])

.controller('StartSoknadCtrl', function($scope, soknadService) {
	$scope.startSoknad = function() {
		console.log("START SOKNAD");
		var soknadType = window.location.pathname.split("/")[3];
		$scope.soknad = soknadService.create({param: soknadType}).$promise.then(function(result) {
			$scope.soknad.id = result.id;
			console.log($scope.soknad.id + "iiiiiiiiiiiid");
		});
		console.log("var : " + $scope.soknad.id);
		
	}
})

.controller('HentSoknadDataCtrl', function($scope, soknadService){
	

})

.controller('SoknadDataCtrl', function($scope, soknadService, $location, $timeout) {

	console.log('SoknadId: '+  $scope.soknad.id);
	console.log("HENT SOKNAD");
	$scope.soknadData = soknadService.get({id:  $scope.soknad.id});	

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

