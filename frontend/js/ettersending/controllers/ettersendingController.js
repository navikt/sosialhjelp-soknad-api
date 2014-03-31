angular.module('nav.ettersending', [])
    .controller('EttersendingCtrl', ['$scope', '$location', 'data', 'ettersendingService', function ($scope, $location, data, ettersendingService) {
        var innsendtDato = new Date(parseInt(data.finnFaktum('soknadInnsendingsDato').value));
        var fristDato = new Date();
        fristDato.setDate(innsendtDato.getDate() + 40);

        $scope.informasjon = {
            innsendtDato: innsendtDato,
            fristDato: fristDato
        };

        $scope.vedlegg = {
            ettersendes: data.soknad.vedlegg.filter(skalEttersendes),
            resten: data.soknad.vedlegg.filter(skalIkkeEttersendes)
        };

        console.log($scope.vedlegg);

        $scope.erLastetOpp = function (v) {
            return v.innsendingsvalg === 'LastetOpp';
        };

        $scope.erIkkeLastetOpp = function (v) {
            return !$scope.erLastetOpp(v);
        };

        function skalEttersendes(v) {
            return v.opprinneligInnsendingsvalg === 'SendesSenere';
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
            ettersendingService.send({},
                {behandlingskjedeId: behandlingsId, soknadId: data.soknad.soknadId},
                function(result) {
                    console.log("done");
                }
            );
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
