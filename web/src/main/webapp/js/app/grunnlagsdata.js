'use strict';

angular.module('app.grunnlagsdata', ['app.services'])

.controller('GrunnlagsdataCtrl', ['$scope', 'grunnlagsdataService', 'soknadService', '$location', '$q', function($scope, grunnlagsdataService, soknadService, $location, $q) {
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
        var soknadType = window.location.pathname.split("/")[3];
        var id = soknadService.create({param: soknadType}).$promise.then(function(result) {
            console.log(result);
        });

    }

	$scope.arena = {
		jobbsoker: true
	}

	$scope.checkUtslagskriterier = function() {
		if($scope.isGyldigAlder() && $scope.arena.jobbsoker && !$scope.borIUtlandet() && $scope.fattDagpengerSisteAaret() === true) {
			$location.path("/informasjonsside");
		}
	}

	$scope.isGyldigAlder = function() {
		return ($scope.personalia.alder >= $scope.minAlder && $scope.personalia.alder < $scope.maxAlder);
	};
	$scope.borIUtlandet = function() {
		return ($scope.midlertidigAdresse.land != 'norge');
	};

	$scope.faarForskuddsvisUtbetalingPgaKonkurs = function() {
		return true;
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
//		if(tiltak) {
//			return true
//		}
//		if(verneplikt) {
//			return true
//		}
//		if(graviditesrelatertSykdom) {
//			return true
//		}
//		if(graviditesrelatertSykdomMedForeldrepenger) {
//			return true
//		}
}
$scope.fattDagpengerSisteAaret = function() {
	var fattDagpenger = true;
	if(!fattDagpenger) {
		$scope.sokePaaNytt();
	}

	if($scope.faarForskuddsvisUtbetalingPgaKonkurs()){
		return "*Forenklet søknad pga konkurs*";
	}
	if(!$scope.hattPermitering()){
		return "*Gjenopptak pga ikke hatt permitering*"
	}
	if(!$scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker() || !($scope.erFisker() && $scope.jobetMerEnn26Uker())){
		return "*Gjennopptak pga ikke hatt jobb hos samme arbeidsgiver vedkommende ble permitert fra i mer enn 6 uker eller fisker"
	}
	if(!$scope.avbruddPgaUtdanning()){
		return "*Gjennopptak pga ikke avbrudd pga utdanning"
	}
	return true;
};

}])