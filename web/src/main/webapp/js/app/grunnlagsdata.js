angular.module('app.grunnlagsdata', ['app.services'])

.controller('GrunnlagsdataCtrl', function($scope, $routeParams, grunnlagsdataService) {
	$scope.personalia = grunnlagsdataService.get();

	$scope.arena = {
	    jobbsoker: false
	  }

	  $scope.checkUtslagskriterier = function() {
	    if($scope.isGyldigAlder() && $scope.arena.jobbsoker) {
	      $location.path("/dagpenger");
	    }
	  }

	  $scope.isGyldigAlder = function() {
	    return ($scope.personalia.alder >= $scope.minAlder && $scope.personalia.alder < $scope.maxAlder);
	  };
})