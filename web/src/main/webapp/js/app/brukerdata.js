angular.module('app.brukerdata', ['app.services'])

.controller('SoknadDataCtrl', function($scope, soknadService, $location, $timeout) {
    var soknadId = window.location.pathname.split("/")[3];
	$scope.soknadData = soknadService.get({id: soknadId});


	$scope.lagre = function() {

		var soknadData = $scope.soknadData;
		console.log(soknadData);
		soknadData.$save({id: soknadData.soknadId});
	}
	 $scope.startSoknad = function() {
        var soknadType = window.location.pathname.split("/")[3];
        var id = soknadService.create({param: soknadType}).$promise.then(function(result) {
            console.log(result);
        });
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

