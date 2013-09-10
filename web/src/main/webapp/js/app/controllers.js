'use strict';

/* Controllers */
function PersonaliaCtrl($scope, HentSoknadService){
	$scope.personalia = HentSoknadService.get({id: 1});
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

function ArbeidsforholdCtrl($scope){
  $scope.arbeidsforhold = {
    arbeidsgiverNavn: '',
    varighetFra: '',
    varighetTil:'',
    land:'',
    sluttaarsaken: ''
   };

   $scope.sluttaarsak = [
   {id: 1, navn:'Sagt opp selv'},
   {id: 2, navn:'Sagt opp av arbeidsgiver'},
   {id: 3, navn:'Avskjediget'},
   {id: 4, navn:'Kontrakt utgått'},
   {id: 5, navn:'Permitert'},
   {id: 6, navn:'Konkurs'},
   {id: 7, navn:'Redusert arbeidstid med mer enn 50% (40% for fiskere)'}
   ];
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
