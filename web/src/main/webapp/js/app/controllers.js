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
    landskode:'+47',
    barnUtland:'',
    barnUtlandNavn: '',
    barnUtlandEtternavn: ''
	};
}

function VilligCtrl($scope){
  $scope.villig = {
    jobb: '',
    pendle: '',
    deltid:'',
    helse:'',
    deltidProsent:''
  }
}

function ValidationCtrl($scope, soknadService, $location){
  //TODO: loop alle verdier i soknadService og sett de.
  //$scope.ePost = soknadService.ePost;
  //$scope.fields = soknadService;
  
  $scope.data =  soknadService.data;

  $scope.feilmeldinger = {
    paakreves: '*',
    feil: 'Ikke gyldig'
  }

  $scope.saveState = function() {
    soknadService.data = $scope.data;
     //TODO: loop alle .skjemaElement og lagre det i soknadService
  }

  $scope.setRoute = function(route) {
      $scope.saveState();
      $location.path(route);
  }
}

function WizardCtrl($scope, soknadService) {
  $scope.data = soknadService.data;
}
