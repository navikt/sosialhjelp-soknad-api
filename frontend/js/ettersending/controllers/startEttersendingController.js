angular.module('nav.ettersending.controllers.start', [])
    .controller('StartEttersendingCtrl', function ($scope, ettersendingService, data, EttersendingMetadataResolver) {
        $scope.mineInnsendinger = data.config["saksoversikt.link.url"];

        $scope.sisteInnsendtBehandling = null;
        $scope.innsendtDato = null;

        var fristDato;
        EttersendingMetadataResolver.then(function(result) {
            var antallDager = data.config["soknad.ettersending.antalldager"];
            $scope.innsendtDato = new Date(parseInt(result.innsendtdato));

            $scope.sisteInnsendtBehandling = result.sisteinnsendtbehandling;
            fristDato = new Date(parseInt(result.innsendtdato));
            fristDato.setDate($scope.innsendtDato.getDate() + parseInt(antallDager));
        });

        $scope.kanStarteEttersending = function () {
            fristDato.setHours(23);
            fristDato.setMinutes(59);
            fristDato.setSeconds(59);

            var idag = new Date();

            return fristDato > idag;
        };

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.startEttersending = function ($event) {
            $event.preventDefault();
            var behandlingId = getBehandlingIdFromUrl();
            $scope.fremdriftsindikator.laster = true;
            ettersendingService.create({},
                {behandlingskjedeId: behandlingId},
                function () {
                    redirectTilSide('/sendsoknad/ettersending/' + behandlingId + '#/vedlegg');
                },
                function () {
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };
    }
);
