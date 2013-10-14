'use strict';

angular.module('app.grunnlagsdata', ['app.services'])
.controller('GrunnlagsdataCtrl', ['$scope', 'utslagskriterierService', 'soknadService', '$location', 'data', function($scope, utslagskriterierService, soknadService, $location, data) {
	$scope.personalia = utslagskriterierService.get();
        $scope.tekster = data.tekster;
//    $scope.utslagskritererTekster = tekstService.get({side:'utslagskriterier'});
	$scope.maxAlder = 67;
	$scope.minAlder = 18;

	$scope.arena = {
		jobbsoker: false
	}

	$scope.checkUtslagskriterier = function() {
		if($scope.personalia.alder && !$scope.personalia.borIUtland) {
			$location.path("informasjonsside");
		}
	}
	
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