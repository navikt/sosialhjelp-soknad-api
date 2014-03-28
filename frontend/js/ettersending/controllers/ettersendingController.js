angular.module('nav.ettersending', [])
    .controller('EttersendingCtrl', ['$scope', '$location', 'data', 'ettersendingService', function ($scope, $location, data, ettersendingService, vedleggService) {
        var innsendtDato = new Date(parseInt(data.finnFaktum('soknadInnsendingsDato').value));
        var fristDato = new Date();
        fristDato.setDate(innsendtDato.getDate() + 40);

        $scope.informasjon = {
            innsendtDato: innsendtDato,
            fristDato: fristDato
        };

        console.log(data.soknad.vedlegg);

        $scope.vedlegg = {
            ettersendes: data.soknad.vedlegg.filter(skalEttersendes),
            resten: data.soknad.vedlegg.filter(skalIkkeEttersendes)
        };

        $scope.erLastetOpp = function (v) {
            return erLastetOpp(v);
        };

        $scope.erIkkeLastetOpp = function (v) {
            return !$scope.erLastetOpp(v);
        };

        function erLastetOpp(v) {
            return v.innsendingsvalg === 'LastetOpp';
        }

        function erLastetOppIDenneInnsendingen(v) {
            return erLastetOpp(v) && v.storrelse > 0;
        }

        function skalEttersendes(v) {
            if (v.opprinneligInnsendingsvalg === 'SendesSenere') {
                return true;
            } else if (erLastetOppIDenneInnsendingen(v)) {
                return true;
            }

            return false;
        }

        function skalIkkeEttersendes(v) {
            return !skalEttersendes(v);
        }

        $scope.erEkstraVedlegg = function (v) {
            return v.skjemaNummer === 'N6';
        };

        $scope.hentTekstKey = function(v) {
            if (v.innsendingsvalg === 'SendesIkke') {
                return 'ettersending.vedlegg.sendesIkke';
            } else if (v.innsendingsvalg === 'LastetOpp') {
                return 'ettersending.vedlegg.sendtInn';
            }
        };

        $scope.hentLenkeKey = function(v) {
            if (v.innsendingsvalg === 'SendesIkke') {
                return 'ettersending.vedlegg.endre';
            } else if (v.innsendingsvalg === 'LastetOpp') {
                return 'ettersending.vedlegg.lastOppIgjen';
            }
        };

        $scope.sendEttersending = function() {

            var behandlingsId = getBehandlingIdFromUrl();
            ettersendingService.send({behandlingsId: behandlingsId},
                function(result) {
                    console.log("done");
                }
            );
        };

        $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen = function() {
            return data.soknad.vedlegg.filter(function(v) {
                return v.storrelse > 0;
            }).length;
        };

        $scope.harLastetOppDokument = function () {
            var antallOpplastedeVedlegg = $scope.hentAntallVedleggSomErOpplastetIDenneEttersendingen();
            return antallOpplastedeVedlegg > 0;
        };

        $scope.nyttAnnetVedlegg = function () {
//            var nyttVedlegg = new vedleggService();
//            nyttVedlegg.soknad
//            new Faktum({
//                key: 'ekstraVedlegg',
//                value: 'true',
//                soknadId: data.soknad.soknadId
//            }).$save().then(function (nyttfaktum) {
//                    vedleggService.hentAnnetVedlegg({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (forventninger) {
//                        $scope.forventninger.push(forventninger);
//                    });
//                });
        };
    }])
    .controller('EttersendingOpplastingCtrl', ['$scope', 'data', '$routeParams', 'vedleggService', function($scope, data, $routeParams, vedleggService) {

        data.soknad.vedlegg.forEach(function(v) {
            if (v.vedleggId == $routeParams.vedleggId) {
                $scope.vedlegg = new vedleggService(v);
            }
        });

        $scope.ettersend = {
            valgt: $scope.vedlegg.innsendingsvalg === 'SendesSenere'
        };

        $scope.settTilEttersendes = function() {
            if ($scope.ettersend.valgt) {
                $scope.vedlegg.innsendingsvalg = 'SendesSenere';
            } else {
                $scope.vedlegg.innsendingsvalg = $scope.vedlegg.opprinneligInnsendingsvalg;
            }

            $scope.vedlegg.$save();
        };

        $scope.kanEndreInnsendingsvalg = function() {
            return $scope.vedlegg.opprinneligInnsendingsvalg === 'SendesIkke';
        };
    }]);
