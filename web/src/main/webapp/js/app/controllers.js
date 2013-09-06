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

function ValidationCtrl($scope){
  $scope.ePost ="";
  $scope.feilmeldinger = {
    paakreves: 'Må fylles ut',
    feil: 'Ikke gyldig'
  };
}
