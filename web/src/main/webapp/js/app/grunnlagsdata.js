'use strict';

angular.module('app.grunnlagsdata', ['app.services'])
.controller('GrunnlagsdataCtrl', ['$scope', 'grunnlagsdataService', 'soknadService', '$location', '$q', function($scope, grunnlagsdataService, soknadService, $location, $q) {
    $scope.personalia = grunnlagsdataService.get();

	$scope.minAlder=18;
	$scope.maxAlder=67;

	$scope.arena = {
		jobbsoker: true
	}

	$scope.checkUtslagskriterier = function() {
		if($scope.isGyldigAlder() && !$scope.borIUtlandet() && $scope.kvalifisererForGjenopptak() === false && $scope.arena.jobbsoker) {
			$location.path("informasjonsside");
		}
	}

	$scope.isGyldigAlder = function() {
		return ($scope.personalia.alder >= $scope.minAlder && $scope.personalia.alder < $scope.maxAlder);
	};
	$scope.borIUtlandet = function() {
		return ($scope.personalia.midlertidigadresseLandkode != 'NOR');
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
		return true;
	};

	$scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker = function() {
		return true;
	};

	$scope.erFisker = function() {
		return true;
	};

	$scope.jobetMerEnn26Uker = function() {
		return true;
	};
	$scope.avbruddPgaUtdanning = function() {
		return true;
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
	if($scope.fattDagpengerSisteAaret()){
		if(!$scope.hattPermitering()){
			return "*Gjenopptak pga ikke hatt permitering, og fått dagpenger de siste 52 ukene.*"
		}
		if(!$scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker()){
			return "*Gjennopptak pga ikke hatt jobb hos samme arbeidsgiver vedkommende ble permitert fra, i mer enn 6 uker, og fått dagpenger de siste 52 ukene."
		}
		if($scope.erFisker() && !$scope.jobetMerEnn26Uker()) {
			return "*Gjennopptak pga fisker, og fått dagpenger de siste 52 ukene."
		}
		if(!$scope.avbruddPgaUtdanning()){
			return"*Gjennopptak pga ikke avbrudd pga utdanning, og fått dagpenger de siste 52 ukene."
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