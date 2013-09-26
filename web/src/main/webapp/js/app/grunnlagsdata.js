'use strict';

angular.module('app.grunnlagsdata', ['app.services'])

.controller('GrunnlagsdataCtrl', ['$scope', 'grunnlagsdataService', '$location', function($scope, grunnlagsdataService, $location) {
	$scope.personalia = grunnlagsdataService.get();
	$scope.minAlder=18;
	$scope.maxAlder=67;

	$scope.folkeregistrertAdresse = {
		gatenavn: 'Majorstuen 1',
		postnummer: '0123',
		poststed: 'Oslo',
		land: 'england'
	}

	$scope.midlertidigAdresse = {
		gatenavn: 'Majorstuen 1',
		postnummer: '0123',
		poststed: 'Oslo',
		land: 'norge'
	}

	$scope.startSoknad = function() {

	}

	$scope.arena = {
		jobbsoker: true
	}

	$scope.checkUtslagskriterier = function() {
		if($scope.isGyldigAlder() && !$scope.borIUtlandet() && $scope.kvalifisererForGjenopptak() === false && $scope.arena.jobbsoker) {
			$location.path("#/informasjonsside");
		}
	}

	$scope.isGyldigAlder = function() {
		return ($scope.personalia.alder >= $scope.minAlder && $scope.personalia.alder < $scope.maxAlder);
	};
	$scope.borIUtlandet = function() {
		return ($scope.midlertidigAdresse.land != 'norge');
	};

	$scope.fattDagpengerSisteAaret = function() {
		var fattDagpenger = true;
		if(!fattDagpenger) {
			$scope.sokePaaNytt();
		}
		return fattDagpenger;
	};

	$scope.faarForskuddsvisUtbetalingPgaKonkurs = function() {
		var faarForskuddsvisUtbetalingPgaKonkurs = true;
		if(faarForskuddsvisUtbetalingPgaKonkurs){
			return "*Forenklet søknad pga konkurs*";
		}
	};

	$scope.hattPermitering = function() {
		return false;
	};

	$scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker = function() {
		return false;
	};

	$scope.erFisker = function() {
		return true;
	};

	$scope.jobetMerEnn26Uker = function() {
		return false;
	};
	$scope.avbruddPgaUtdanning = function() {
		return false;
	};

	$scope.sokePaaNytt = function() {
//		if(tidligere tiltak) {
//			return "*Gjenopptak pga tidligere fått invilget tiltakspenger"
//		}
//		if(tidligere verneplikt) {
//			return "*Gjenopptak pga tidligere fått invilget vernepliktspenger"
//		}
//		if(tidligere graviditesrelatertSykdom) {
//			return "*Gjenopptak pga tidligere fått invilget graviditesrelatertSykdomspenger"
//		}
//		if(tidligere graviditesrelatertSykdomMedForeldrepenger) {
//			return "*Gjenopptak pga tidligere fått invilget graviditesrelatertSykdomMedForeldrepenger"
//		}
return false;
};


$scope.kvalifisererForGjenopptak = function() {
	if(fattDagpengerSisteAaret){
		if(!$scope.hattPermitering()){
			return "*Gjenopptak pga ikke hatt permitering*"
		}
		if(!$scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker()){
			return "*Gjennopptak pga ikke hatt jobb hos samme arbeidsgiver vedkommende ble permitert fra, i mer enn 6 uker"
		}
		if($scope.erFisker() && !$scope.jobetMerEnn26Uker()) {
			return "*Gjennopptak pga fisker"
		}
		if(!$scope.avbruddPgaUtdanning()){
			return"*Gjennopptak pga ikke avbrudd pga utdanning"
		}
	}
	else {
		if(sokePaaNytt() != false){
			return sokePaaNytt();
		}
	}
	return false;
};
}])