angular.module('nav.utslagskriterierDagpenger', [])
    .controller('utslagskritererDagpengerCtrl', ['$scope', 'data', '$location', 'soknadService', 'sjekkUtslagskriterier', function ($scope, data, $location, soknadService, sjekkUtslagskriterier) {
        $scope.utslagskriterier = data.utslagskriterier;
        $scope.utslagskriterier.harlestbrosjyre = false;

        $scope.alderspensjonUrl = data.config["soknad.alderspensjon.url"];
        $scope.saksoversiktUrl = data.config["saksoversikt.link.url"];
        $scope.reelArbeidsokerUrl = data.config["soknad.reelarbeidsoker.url"];
        $scope.dittnavUrl = data.config["dittnav.link.url"];

        $scope.oppsummering = false;

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.hentAdresseLinjer = function () {
            if (isNotNullOrUndefined($scope.utslagskriterier.registrertAdresse)) {
                return $scope.utslagskriterier.registrertAdresse.split(", ");
            }
            return [];
        };

        $scope.tpsSvarer = function () {
            return !$scope.tpsSvarerIkke();
        };

        $scope.tpsSvarerIkke = function () {
            if ($scope.utslagskriterier.error !== undefined) {
                return true;
            }
            return false;
        };

        $scope.soknadErIkkeFerdigstilt = function () {
            return !$scope.soknadErFerdigstilt();
        };

        $scope.soknadErFerdigstilt = function () {
            return data && data.soknad && data.soknad.status === "FERDIG";
        };

        //slutt

        $scope.fortsettLikevel = function ($event) {
            $event.preventDefault();
            tilRoutingForGjenopptak();
        };

        $scope.kravForDagpengerOppfylt = function () {
            if (sjekkUtslagskriterier.erOppfylt()) {
                $location.path("/routing");
            }
        };

        $scope.kravForDagpengerIkkeOppfylt = function () {
            return !$scope.kravForDagpengerOppfylt() && $scope.soknadErIkkeFerdigstilt();
        };

        $scope.gyldigAlder = function () {
            return sjekkUtslagskriterier.harGyldigAlder();
        };

        $scope.bosattINorge = function () {
            return sjekkUtslagskriterier.erBosattINorge();
        };

        $scope.registrertArbeidssoker = function () {
            return sjekkUtslagskriterier.erRegistrertArbeidssoker();
        };

        $scope.ikkeRegistrertArbeidssoker = function () {
            return sjekkUtslagskriterier.erIkkeRegistrertArbeidssoker();
        };

        $scope.registrertArbeidssokerUkjent = function () {
            return sjekkUtslagskriterier.harUkjentStatusSomArbeidssoker();
        };

        $scope.ikkeGyldigAlder = function () {
            return !$scope.gyldigAlder();
        };

        $scope.ikkeBosattINorge = function () {
            return !$scope.bosattINorge();
        };

        function tilRoutingForGjenopptak() {
            $location.path("/routing");
        }
    }])
    .factory('sjekkUtslagskriterier', ['data', function (data) {
        function registrertArbeidssoker() {
            return data.utslagskriterier.registrertArbeidssøker === 'REGISTRERT';
        }

        function ikkeRegistertArbeidssoker() {
            return data.utslagskriterier.registrertArbeidssøker === 'IKKE_REGISTRERT';
        }

        function gyldigAlder() {
            return data.utslagskriterier.gyldigAlder === 'true';
        }

        function bosattINorge() {
            return data.utslagskriterier.bosattINorge === 'true';
        }

        return {
            erOppfylt: function () {
                return registrertArbeidssoker() && gyldigAlder() && bosattINorge();
            },

            harGyldigAlder: function () {
                return gyldigAlder();
            },

            erBosattINorge: function () {
                return bosattINorge();
            },

            erRegistrertArbeidssoker: function () {
                return registrertArbeidssoker();
            },

            erIkkeRegistrertArbeidssoker: function () {
                return ikkeRegistertArbeidssoker();
            },

            harUkjentStatusSomArbeidssoker: function () {
                return !ikkeRegistertArbeidssoker() && !registrertArbeidssoker();
            }
        };
    }]);