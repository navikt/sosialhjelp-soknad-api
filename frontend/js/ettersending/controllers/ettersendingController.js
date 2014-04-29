angular.module('nav.ettersending.controllers.main', [])
    .controller('EttersendingCtrl', function ($scope, data, ettersendingService, vedleggService, Faktum, vedlegg, $location) {
        var antallDager = data.config["soknad.ettersending.antalldager"];
        var innsendtDato = new Date(parseInt(data.finnFaktum('soknadInnsendingsDato').value));
        var fristDato = new Date(innsendtDato.getTime());
        fristDato.setDate(innsendtDato.getDate() + parseInt(antallDager));

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.informasjon = {
            innsendtDato: innsendtDato,
            fristDato: fristDato
        };

        $scope.ikkeOpplatetDokumenter = false;

        $scope.vedlegg = vedlegg;

        $scope.erLastetOpp = function (v) {
            return erLastetOppIDenneInnsendingen(v);
        };

        $scope.erIkkeLastetOpp = function (v) {
            return !$scope.erLastetOpp(v);
        };

        $scope.erAnnetVedlegg = function (v) {
            return erAnnetVedlegg(v);
        };

        function erLastetOpp(v) {
            return v.innsendingsvalg === 'LastetOpp';
        }

        function erLastetOppIDenneInnsendingen(v) {
            return erLastetOpp(v) && v.storrelse > 0;
        }

        function erAnnetVedlegg(v) {
            return v.skjemaNummer === "N6" && v.opprinneligInnsendingsvalg === null;
        }

        $scope.hentTekstKey = function (v) {
            if (v.opprinneligInnsendingsvalg === 'SendesIkke') {
                return 'ettersending.vedlegg.sendesIkke';
            } else if (v.opprinneligInnsendingsvalg === 'LastetOpp') {
                return 'ettersending.vedlegg.sendtInn';
            }
        };

        $scope.hentLenkeKey = function (v) {
            if (v.opprinneligInnsendingsvalg === 'SendesIkke') {
                return 'ettersending.vedlegg.endre';
            } else if (v.opprinneligInnsendingsvalg === 'LastetOpp') {
                return 'ettersending.vedlegg.lastOpp';
            }
        };

        $scope.sendEttersending = function () {
            var opplastedeVedlegg = vedlegg.filter(function(v) {
                return v.storrelse > 0;
            });

            if (opplastedeVedlegg.length > 0) {
                $scope.fremdriftsindikator.laster = true;
                ettersendingService.send({soknadId: data.soknad.soknadId},
                    {},
                    function () {
                        $location.path('bekreftelse/' + data.soknad.brukerBehandlingId);
                        $scope.fremdriftsindikator.laster = false;
                    },
                    function() {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            } else {
                $scope.ikkeOpplatetDokumenter = true;
            }

        };

        $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen = function () {
            return vedlegg.filter(function (v) {
                return v.storrelse > 0;
            }).length;
        };

        $scope.harLastetOppDokument = function () {
            var antallOpplastedeVedlegg = $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen();
            return antallOpplastedeVedlegg > 0;
        };

        $scope.slettVedlegg = function (v) {
            var vedlegg = new vedleggService(v);

            vedlegg.$remove().then(function () {
                v.innsendingsvalg = v.opprinneligInnsendingsvalg;
                v.storrelse = 0;
            });
        };

        $scope.slettAnnetVedlegg = function (v) {
            var index = vedlegg.indexOf(v);
            Faktum.delete({soknadId: v.soknadId, faktumId: v.faktumId});
            vedlegg.splice(index, 1);
        };

        $scope.scrollTilElement = function(element) {
            scrollToElement(element, 0);
        };
    })
    .filter('ettersendes', function () {
        return function (input, ettersendesBolk) {
            function skalEttersendes(v) {
                if (v.opprinneligInnsendingsvalg === 'SendesSenere') {
                    return true;
                } else if (erLastetOppIDenneInnsendingen(v)) {
                    return true;
                } else if (erAnnetVedleggSomErLagtTilIDenneInnsendingen(v)) {
                    return true;
                }

                return false;
            }

            function skalIkkeEttersendes(v) {
                return !skalEttersendes(v);
            }

            function erLastetOpp(v) {
                return v.innsendingsvalg === 'LastetOpp';
            }

            function erLastetOppIDenneInnsendingen(v) {
                return erLastetOpp(v) && v.storrelse > 0;
            }

            function erAnnetVedlegg(v) {
                return v.skjemaNummer === "N6";
            }

            function erAnnetVedleggSomErLagtTilIDenneInnsendingen(v) {
                return erAnnetVedlegg(v) && v.opprinneligInnsendingsvalg === null;
            }

            if (ettersendesBolk) {
                return input.filter(skalEttersendes);
            } else {
                return input.filter(skalIkkeEttersendes);
            }

        };
    });
