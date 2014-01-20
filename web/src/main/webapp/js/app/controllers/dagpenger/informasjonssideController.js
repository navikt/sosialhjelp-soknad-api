angular.module('nav.informasjonsside', ['nav.cmstekster'])
	.controller('InformasjonsSideCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', 'soknadService', function ($scope, data, $routeParams, $http, $location, soknadService) {
			$scope.utslagskriterier = data.utslagskriterier;
			//Inntil vi får arena-kobling
			$scope.utslagskriterier.erRegistrertArbeidssoker = "true";
			$scope.utslagskriterier.harlestbrosjyre=false;
			//For testing uten TPS:
			
			//$scope.utslagskriterier.gyldigAlder = true;
			//$scope.utslagskriterier.bosattINorge = false;

			$scope.gjeldendeAdresse = angular.fromJson($scope.utslagskriterier.registrertAdresse);

			$scope.skalViseBrosjyreMelding = false;
			
			$scope.fremdriftsindikator = {
				laster: false
			};

			$scope.tpsSvarer = function() {
				return !$scope.tpsSvarerIkke()
			}

			$scope.tpsSvarerIkke = function() {
				if($scope.utslagskriterier.error != undefined) {
					return true;
				}
				return false;
			}

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

			$scope.fortsettLikevel = function($event) {
				$event.preventDefault();
				$scope.utslagskriterier.erRegistrertArbeidssoker = 'true';
				$scope.utslagskriterier.gyldigAlder = 'true';
				$scope.utslagskriterier.bosattINorge = 'true';
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
				return $scope.registrertArbeidssoker() && $scope.gyldigAlder() && $scope.bosattINorge();
			};

			$scope.kravForDagpengerIkkeOppfylt = function () {
				return !$scope.kravForDagpengerOppfylt();
			};


			$scope.registrertArbeidssoker = function () {
				return $scope.utslagskriterier.erRegistrertArbeidssoker == 'true'
			};

			$scope.gyldigAlder = function () {
				return $scope.utslagskriterier.gyldigAlder == 'true';
			};

			$scope.bosattINorge = function () {
				return $scope.utslagskriterier.bosattINorge == 'true';
			};

			$scope.ikkeRegistrertArbeidssoker = function () {
				return !$scope.registrertArbeidssoker();
			};

			$scope.ikkeGyldigAlder = function () {
				return !$scope.gyldigAlder();
			};

			$scope.ikkeBosattINorge = function () {
				return !$scope.bosattINorge();
			};
		}]);
