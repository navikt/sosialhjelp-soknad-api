angular.module('app.grunnlagsdata', ['ngResource'])

.controller('GrunnlagsdataCtrl', function($scope, $routeParams, grunnlagsdataService) {
	$scope.personalia = grunnlagsdataService.get({id: 12345612345});

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


.factory('grunnlagsdataService', function($resource){
	return $resource('/sendsoknad/rest/grunnlagsdata/:id', {id: '@id'});
})