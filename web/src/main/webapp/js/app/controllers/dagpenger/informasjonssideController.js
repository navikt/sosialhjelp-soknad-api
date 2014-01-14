angular.module('nav.informasjonsside', ['nav.cmstekster'])
	.controller('InformasjonsSideCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', 'soknadService', function ($scope, data, $routeParams, $http, $location, soknadService) {
			$scope.utslagskriterier = data.utslagskriterier;
			//Inntil vi får arena-kobling
			$scope.utslagskriterier.erRegistrertArbeidssoker = true;
			$scope.utslagskriterier.harlestbrosjyre=false;
			//For testing uten TPS:
			/*  $scope.utslagskriterier.gyldigAlder = true;
			 $scope.utslagskriterier.bosattINorge = false;*/


			$scope.skalViseBrosjyreMelding = false;

			$scope.fremdriftsindikator = {
				laster: false
			};
	        $scope.startSoknad = function () {
	            var soknadType = window.location.pathname.split("/")[3];
	            $scope.fremdriftsindikator.laster = true;
	            $scope.soknad = soknadService.create({param: soknadType},
	                function (result) {
	                    $location.path('dagpenger/' + result.id);
	                }, function () {
	                    $scope.fremdriftsindikator.laster = false;
	                });
	        }

			$scope.harLestBrosjyre = function() {
				return $scope.utslagskriterier.harlestbrosjyre;
			}

			$scope.startSoknadDersomBrosjyreLest = function() {
				if($scope.harLestBrosjyre()) {
					$scope.skalViseBrosjyreMelding = false;
					$scope.startSoknad();
				} else {
					$scope.skalViseBrosjyreMelding=true;
					//alert("oioi, du må nok lese brosjyren først!")
				}
			}
			$scope.kravForDagpengerOppfylt = function () {
				return $scope.utslagskriterier.erRegistrertArbeidssoker && $scope.utslagskriterier.gyldigAlder && $scope.utslagskriterier.bosattINorge;
			};

			$scope.kravForDagpengerIkkeOppfylt = function () {
				return !$scope.kravForDagpengerOppfylt();
			};

			$scope.ikkeRegistrertArbeidssoker = function () {
				return !$scope.utslagskriterier.erRegistrertArbeidssoker
			};

			$scope.ikkeGyldigAlder = function () {
				return !$scope.utslagskriterier.gyldigAlder;
			};

			$scope.ikkeBosattINorge = function () {
				return !$scope.utslagskriterier.bosattINorge;
			};
		}]);
