angular.module('nav.informasjonsside',['nav.cmstekster'])
    .controller('InformasjonsSideCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', 
        function ($scope, data, $routeParams, $http,  $location) {
            $scope.utslagskriterier = data.utslagskriterier;
            //Inntil vi får arena-kobling
            $scope.utslagskriterier.erRegistrertArbeidssoker = true;
            //For testing uten TPS:
            /* $scope.utslagskriterier.gyldigAlder = true;
            $scope.utslagskriterier.bosattINorge = false;*/
            

        $scope.kravForDagpengerOppfylt = function() {
            return $scope.utslagskriterier.erRegistrertArbeidssoker && $scope.utslagskriterier.gyldigAlder && $scope.utslagskriterier.bosattINorge;
        }

        $scope.kravForDagpengerIkkeOppfylt = function() {
            return !$scope.kravForDagpengerOppfylt();
        }

        $scope.ikkeRegistrertArbeidssoker = function() {
            return !$scope.utslagskriterier.erRegistrertArbeidssoker
        }
        
        $scope.ikkeGyldigAlder = function() {
            return !$scope.utslagskriterier.gyldigAlder;
        }
        
        $scope.ikkeBosattINorge = function() {
            return !$scope.utslagskriterier.bosattINorge;
        }
    }])