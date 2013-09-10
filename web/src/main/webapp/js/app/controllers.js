'use strict';

/* Controllers */
function PersonaliaCtrl($scope, $location){
	$scope.minAlder=18;
  $scope.maxAlder=67;

  $scope.personalia = {
		fornavn: 'Ingvild',
    etternavn: 'Indrebø',
    alder: 67,
    fnr: '12345123456',
    postnummer: '0123',
    poststed: 'Oslo',
    adresse: 'Majorstuen 1',
    landskode:'+47',
    barnUtland:''
	};

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
}

function ungerUtlandCtrl($scope){
  $scope.ungerUtland = [{
    fornavn: '',
    etternavn: ''
  }];

  $scope.nyUngeUtland = function($event){
    $scope.ungerUtland.push( {fornavn: '', etternavn: ''});
    $event.preventDefault();
  }

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
  $scope.data =  soknadService.data;

  $scope.feilmeldinger = {
    paakreves: '*',
    feil: 'Ikke gyldig'
  }

  $scope.saveState = function() {
    soknadService.data = $scope.data;
  }

  $scope.setRoute = function(route) {
      $scope.saveState();
      $location.path(route);
  }
}
