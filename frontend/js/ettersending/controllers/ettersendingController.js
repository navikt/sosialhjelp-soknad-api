angular.module('nav.ettersending.controllers.main', [])
    .controller('EttersendingCtrl', function ($scope, data, ettersendingService, vedleggService, Faktum, vedlegg, $location, sjekkOmSkalEttersendes, cms, $filter) {
        var antallDagerFristKey = 'ettersending.soknadsfrist.' + trimWhitespaceIString(data.soknad.skjemaNummer.toLowerCase());
        var defaultAntallDagerFristKey = 'ettersending.soknadsfrist.default';
        var antallDager = cms[antallDagerFristKey];

        if (antallDager === undefined) {
            antallDager = cms[defaultAntallDagerFristKey];
        }

        var innsendtDato = new Date(parseFloat(data.finnFaktum('soknadInnsendingsDato').value));
        var fristDato = new Date(innsendtDato.getTime());
        fristDato.setDate(innsendtDato.getDate() + parseInt(antallDager));

        fristDato = $filter('date')(fristDato, 'dd.MM.yyyy');
        fristDato = $filter('norskdato')(fristDato);

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.informasjon = {
            innsendtDato: innsendtDato,
            frist: [antallDager, fristDato]
        };

        $scope.ikkeOpplatetDokumenter = false;
        $scope.ikkeOpplastetAnnetVedlegg = false;

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

        $scope.erLastetoppOgIkkeAnnetVedleggLagtTilIDenneBehandlingen = function(v) {
            return !erAnnetVedleggLagtTilIDenneInnsendingen(v) && $scope.erLastetOpp(v);
        };

        $scope.erAnnetVedleggLagtTilIDenneInnsendingen = function (v) {
            return erAnnetVedleggLagtTilIDenneInnsendingen(v);
        };

        function erLastetOpp(v) {
            return v.innsendingsvalg === 'LastetOpp';
        }

        function erLastetOppIDenneInnsendingen(v) {
            return erLastetOpp(v) && v.storrelse > 0;
        }

        function erAnnetVedleggLagtTilIDenneInnsendingen(v) {
            return v.skjemaNummer === "N6" && v.opprinneligInnsendingsvalg === null;
        }

        function erAnnetVedlegg(v) {
            return v.skjemaNummer === "N6";
        }

        function harAnnetVedleggSomIkkeErLastetOpp() {
            return vedlegg.filter(function (v) {
                return erAnnetVedleggLagtTilIDenneInnsendingen(v) && v.storrelse === 0;
            }).length > 0;
        }

        $scope.harSkjemaLenke = function (v) {
            return v.urls.URL;
        };

        $scope.sendEttersending = function () {
            $scope.ikkeOpplatetDokumenter = false;
            $scope.ikkeOpplastetAnnetVedlegg = false;

            var opplastedeVedlegg = vedlegg.filter(function (v) {
                return v.storrelse > 0;
            });

            if (opplastedeVedlegg.length > 0 && !harAnnetVedleggSomIkkeErLastetOpp()) {
                $scope.fremdriftsindikator.laster = true;
                ettersendingService.send({soknadId: data.soknad.soknadId},
                    {},
                    function () {
                        $location.path('bekreftelse/' + data.soknad.brukerBehandlingId);
                        $scope.fremdriftsindikator.laster = false;
                    },
                    function () {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            } else if(opplastedeVedlegg.length === 0) {
                $scope.ikkeOpplatetDokumenter = true;
            } else {
                $scope.ikkeOpplastetAnnetVedlegg = true;
            }

        };

        $scope.harFeil = function() {
            return $scope.ikkeOpplatetDokumenter || $scope.ikkeOpplastetAnnetVedlegg;
        };

        $scope.skalViseEttersendingsbolk = function() {
            return vedlegg.filter(sjekkOmSkalEttersendes.skalEttersendes).length > 0;
        };

        $scope.skalViseSendtBolk = function() {
            return vedlegg.filter(sjekkOmSkalEttersendes.erSendtInn).length > 0;
        };

        $scope.skalViseSendesIkkeBolk = function() {
            return vedlegg.filter(sjekkOmSkalEttersendes.skalIkkeSendes).length > 0;
        };

        $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen = function () {
            return vedlegg.filter(function (v) {
                return v.storrelse > 0;
            }).length;
        };

        $scope.harLastetOppDokument = function () {
            return $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen() > 0;
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

        $scope.scrollTilElement = function (element) {
            scrollToElement(element, 0);
        };

        $scope.hentTekstKey = function(v) {
            if (v.innsendingsvalg === 'VedleggSendesIkke') {
                return 'ettersending.vedlegg.vedleggSendesIkke';
            } else if (v.innsendingsvalg === 'VedleggSendesAvAndre') {
                return'ettersending.vedlegg.vedleggSendesAvAndre';
            } else {
                // TODO: Denne kan fjernes når det ikke lengre er mulig å sende inn ettersending på søknader som er sendt inn før vi la til sendes av andre
                return 'ettersending.vedlegg.sendesIkke';
            }
        };

        $scope.hentKlareVedleggTekst = function() {
            if ($scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen() > 1) {
                return 'ettersending.vedlegg.klare';
            } else {
                return 'ettersending.vedlegg.klare.ett';
            }

        };
    })
    .filter('ettersendes', function (sjekkOmSkalEttersendes) {
        return function (input) {
            return input.filter(sjekkOmSkalEttersendes.skalEttersendes);
        };
    })
    .filter('sendtInn', function (sjekkOmSkalEttersendes) {
        return function (input) {
            return input.filter(sjekkOmSkalEttersendes.erSendtInn);
        };
    })
    .filter('sendesIkke', function (sjekkOmSkalEttersendes) {
        return function (input) {
            return input.filter(sjekkOmSkalEttersendes.skalIkkeSendes);
        };
    })
    .factory('sjekkOmSkalEttersendes', function () {
        function skalEttersendes(v) {
            if (erLastetOppIDenneInnsendingen(v)) {
                return true;
            } else if (v.opprinneligInnsendingsvalg === 'SendesSenere') {
                return true;
            } else if (erAnnetVedleggSomErLagtTilIDenneInnsendingen(v)) {
                return true;
            }
            return false;
        }

        function erLastetOpp(v) {
            return v.innsendingsvalg === 'LastetOpp' && v.storrelse === 0;
        }

        function erLastetOppIDenneInnsendingen(v) {
            return v.innsendingsvalg === 'LastetOpp' && v.storrelse > 0;
        }

        function erAnnetVedlegg(v) {
            return v.skjemaNummer === "N6";
        }

        function erAnnetVedleggSomErLagtTilIDenneInnsendingen(v) {
            return erAnnetVedlegg(v) && v.opprinneligInnsendingsvalg === null;
        }

        return {
            skalEttersendes: function (v) {
                return skalEttersendes(v);
            },

            erSendtInn: function(v) {
                return erLastetOpp(v);
            },

            skalIkkeSendes: function (v) {
                return !skalEttersendes(v) && !erLastetOpp(v);
            }
        };
    });
