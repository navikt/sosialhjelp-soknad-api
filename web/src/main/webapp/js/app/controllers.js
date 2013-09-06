'use strict';

/* Controllers */
function PersonaliaCtrl($scope){
	$scope.personalia = {
		fornavn: 'Ingvild',
    etternavn: 'Indrebø',
    fnr: '12345123456',
    postnummer: '0123',
    poststed: 'Oslo',
    adresse: 'Majorstuen 1',
    landskode:'+47'
	};
}

function ValidationCtrl($scope, soknadService, $location){
  $scope.ePost = soknadService.ePost;
  $scope.feilmeldinger = {
    paakreves: 'Må fylles ut',
    feil: 'Ikke gyldig'
  }

  $scope.saveState = function() {
    soknadService.ePost = $scope.ePost;
  }

  $scope.setRoute = function(route) {
      $scope.saveState();
      $location.path(route);
  }
  
}

function WizardCtrl($scope, soknadService) {
  $scope.ePost = soknadService.ePost;
}
