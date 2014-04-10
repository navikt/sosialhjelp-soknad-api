angular.module('nav.ettersending', [])
    .controller('EttersendingCtrl', ['$scope', 'data', 'ettersendingService', 'vedleggService', 'Faktum', function ($scope, data, ettersendingService, vedleggService, Faktum) {
        var innsendtDato = new Date(parseInt(data.finnFaktum('soknadInnsendingsDato').value));
        var fristDato = new Date();
        fristDato.setDate(innsendtDato.getDate() + 40);

        $scope.informasjon = {
            innsendtDato: innsendtDato,
            fristDato: fristDato
        };

        $scope.vedlegg = data.soknad.vedlegg;

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
            var behandlingsId = getBehandlingIdFromUrl();
            ettersendingService.send({},
                {behandlingskjedeId: behandlingsId, soknadId: data.soknad.soknadId},
                function (result) {
                    console.log("done");
                }
            );
        };

        $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen = function () {
            return data.soknad.vedlegg.filter(function (v) {
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
            var index = data.soknad.vedlegg.indexOf(v);
            Faktum.delete({soknadId: v.soknadId, faktumId: v.faktumId});
            data.soknad.vedlegg.splice(index, 1);
        };

        $scope.scrollTilElement = function(element) {
            console.log(element);
            scrollToElement(element, 0);
        };
    }])
    .controller('EttersendingOpplastingCtrl', ['$scope', 'data', '$routeParams', 'vedleggService', function ($scope, data, $routeParams, vedleggService) {

        data.soknad.vedlegg.forEach(function (v) {
            if (v.vedleggId == $routeParams.vedleggId) {
                $scope.vedlegg = new vedleggService(v);
            }
        });

        $scope.ettersend = {
            valgt: $scope.vedlegg.innsendingsvalg === 'SendesSenere'
        };

        $scope.settTilEttersendes = function () {
            if ($scope.ettersend.valgt) {
                $scope.vedlegg.innsendingsvalg = 'SendesSenere';
            } else {
                $scope.vedlegg.innsendingsvalg = $scope.vedlegg.opprinneligInnsendingsvalg;
            }

            $scope.vedlegg.$save();
        };

        $scope.kanEndreInnsendingsvalg = function () {
            return $scope.vedlegg.opprinneligInnsendingsvalg === 'SendesIkke';
        };
    }])
    .controller('EttersendingNyttVedleggCtrl', ['$scope', 'data', 'vedleggService', '$location', 'Faktum', function ($scope, data, vedleggService, $location, Faktum) {
        $scope.nyttvedlegg = {};
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.lagreVedlegg = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);
            $scope.runValidation(true);

            if (form.$valid) {

                new Faktum({
                    key: 'ekstraVedlegg',
                    value: 'true',
                    soknadId: data.soknad.soknadId
                }).$save().then(function (nyttfaktum) {
                        vedleggService.hentAnnetVedlegg({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (resultVedlegg) {
                            resultVedlegg.navn = $scope.nyttvedlegg.navn;
                            resultVedlegg.$save();
                            data.soknad.vedlegg.push(resultVedlegg);
                            $location.path('opplasting/' + resultVedlegg.vedleggId);
                        });
                    });
            }
        };
    }])
    .filter('ettersendes', [function () {
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
    }]);
