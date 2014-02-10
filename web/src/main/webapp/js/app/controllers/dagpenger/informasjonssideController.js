angular.module('nav.informasjonsside', ['nav.cmstekster'])
    .controller('FeilSideCtrl', ['$scope', 'data',  function ($scope, data) {

    }])
    .controller('InformasjonsSideCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', 'soknadService', 'sjekkUtslagskriterier', function ($scope, data, $routeParams, $http, $location, soknadService, sjekkUtslagskriterier) {
        var fortsettLikevell = false;

        $scope.utslagskriterier = data.utslagskriterier;
        //Inntil vi får arena-kobling
        $scope.utslagskriterier.erRegistrertArbeidssoker = "true";
        $scope.utslagskriterier.harlestbrosjyre = false;
        //For testing uten TPS:

        //$scope.utslagskriterier.gyldigAlder = false;
        //$scope.utslagskriterier.bosattINorge = false;

        $scope.alderspensjonUrl = data.config["soknad.alderspensjon.url"];
        $scope.mineHenveldelserUrl = data.config["minehenvendelser.link.url"];
        $scope.reelArbeidsokerUrl = data.config["soknad.reelarbeidsoker.url"];
        $scope.dagpengerBrosjyreUrl = data.config["soknad.dagpengerbrosjyre.url"];
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
        $scope.skalViseBrosjyreMelding = false;

        $scope.oppsummering = false;
        if (getBehandlingIdFromUrl() != "Dagpenger") {
            $scope.utslagskriterier.harlestbrosjyre = true;
            $scope.oppsummering = true;
        }

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.tpsSvarer = function () {
            return !$scope.tpsSvarerIkke()
        }

        $scope.tpsSvarerIkke = function () {
            if ($scope.utslagskriterier.error != undefined) {
                return true;
            }
            return false;
        }

        $scope.soknadErIkkeStartet = function () {
            return !$scope.soknadErStartet();
        }

        $scope.soknadErStartet = function () {
            var behandlingId = getBehandlingIdFromUrl();
            if (behandlingId != "Dagpenger") {
                return true;
            }
            return false;
        }

        $scope.soknadErIkkeFerdigstilt = function () {
            return !$scope.soknadErFerdigstilt();
        }

        $scope.soknadErFerdigstilt = function () {
            return data && data.soknad && data.soknad.status == "FERDIG";
        }

        $scope.startSoknad = function () {
            var soknadType = window.location.pathname.split("/")[3];
            $scope.fremdriftsindikator.laster = true;
            $scope.soknad = soknadService.create({soknadType: soknadType},
                function (result) {
                    var currentUrl = location.href;
                    location.href = currentUrl.substring(0, currentUrl.indexOf('start/')) + 'soknad/' + result.brukerbehandlingId + '#/soknad';
                }, function () {
                    $scope.fremdriftsindikator.laster = false;
                });
        }

        $scope.harLestBrosjyre = function () {
            return $scope.utslagskriterier.harlestbrosjyre;
        }

        $scope.fortsettLikevel = function ($event) {
            $event.preventDefault();
            fortsettLikevell = true;
        }

        $scope.startSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $scope.skalViseBrosjyreMelding = false;
                $scope.startSoknad();
            } else {
                $scope.skalViseBrosjyreMelding = true;
            }
        }

        $scope.forsettSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $scope.skalViseBrosjyreMelding = false;
                $location.path("/soknad");
            } else {
                $scope.skalViseBrosjyreMelding = true;
            }
        }

        $scope.kravForDagpengerOppfylt = function () {
            return sjekkUtslagskriterier.erOppfylt() || fortsettLikevell;
        };

        $scope.kravForDagpengerIkkeOppfylt = function () {
            return !$scope.kravForDagpengerOppfylt() && $scope.soknadErIkkeFerdigstilt();
        };


        $scope.registrertArbeidssoker = function () {
            return sjekkUtslagskriterier.erRegistrertArbeidssoker();
        };

        $scope.gyldigAlder = function () {
            return sjekkUtslagskriterier.harGyldigAlder();
        };

        $scope.bosattINorge = function () {
            return sjekkUtslagskriterier.erBosattINorge();
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
    }])
    .factory('sjekkUtslagskriterier', ['data', function (data) {
        function registrertArbeidssoker() {
            return data.utslagskriterier.registrertArbeidssøker === 'REGISTRERT';
        };

        function ikkeRegistertArbeidssoker() {
            return data.utslagskriterier.registrertArbeidssøker === 'IKKE_REGISTRERT';
        };

        function gyldigAlder() {
            return data.utslagskriterier.gyldigAlder === 'true';
        };

        function bosattINorge() {
            return data.utslagskriterier.bosattINorge === 'true';
        };

        return {
            erOppfylt: function() {
                return (registrertArbeidssoker() || !ikkeRegistertArbeidssoker()) && gyldigAlder() && bosattINorge();
            },

            harGyldigAlder: function() {
                return gyldigAlder();
            },

            erBosattINorge: function() {
                return bosattINorge();
            },

            erRegistrertArbeidssoker: function() {
                return registrertArbeidssoker();
            },

            erIkkeRegistrertArbeidssoker: function() {
                return ikkeRegistertArbeidssoker();
            },

            harUkjentStatusSomArbeidssoker: function() {
                return !ikkeRegistertArbeidssoker() && !registrertArbeidssoker();
            }
        }
    }]);
