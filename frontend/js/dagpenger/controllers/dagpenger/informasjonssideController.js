angular.module('nav.informasjonsside', ['nav.cmstekster'])
    .controller('InformasjonsSideCtrl', ['$scope', 'data', '$location', 'soknadService', 'sjekkUtslagskriterier', function ($scope, data, $location, soknadService, sjekkUtslagskriterier) {
        var fortsettLikevell = false;

        $scope.utslagskriterier = data.utslagskriterier;
        $scope.utslagskriterier.harlestbrosjyre = false;

        $scope.alderspensjonUrl = data.config["soknad.alderspensjon.url"];
        $scope.mineHenveldelserUrl = data.config["minehenvendelser.link.url"];
        $scope.reelArbeidsokerUrl = data.config["soknad.reelarbeidsoker.url"];
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];

        $scope.oppsummering = false;
        if (erSoknadStartet()) {
            $scope.utslagskriterier.harlestbrosjyre = true;
            $scope.oppsummering = true;
        }

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

        $scope.soknadErIkkeStartet = function () {
            return !$scope.soknadErStartet();
        };

        $scope.soknadErStartet = function () {
            if (erSoknadStartet()) {
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

        $scope.startSoknad = function () {
            var soknadType = decodeURI(window.location.pathname).split("/")[3];
            $scope.fremdriftsindikator.laster = true;
            $scope.soknad = soknadService.create({soknadType: soknadType},
                function (result) {
                    var currentUrl = location.href;
                    location.href = currentUrl.substring(0, currentUrl.indexOf('start/')) + 'soknad/' + result.brukerbehandlingId + '#/soknad';
                }, function () {
                    $scope.fremdriftsindikator.laster = false;
                });
        };

        $scope.harLestBrosjyre = function () {
            return $scope.utslagskriterier.harlestbrosjyre;
        };

        $scope.fortsettLikevel = function ($event) {
            $event.preventDefault();
            fortsettLikevell = true;
        };

        $scope.startSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $scope.startSoknad();
            }
        };

        $scope.forsettSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $location.path("/soknad");
            }
        };

        $scope.kravForDagpengerOppfylt = function () {
            return sjekkUtslagskriterier.erOppfylt() || fortsettLikevell;
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
    }])
    .directive('validerInformasjonsside', [function () {
        return {
            link: function (scope, element) {
                var formElement = element.closest('form').find('.form-linje');
                var input = formElement.find('input');
                element.bind('click', function () {
                    if (!input.is(':checked')) {
                        formElement.addClass('feilstyling');
                    }
                });
            }
        };
    }]);
