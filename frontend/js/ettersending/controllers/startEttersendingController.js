angular.module('nav.ettersending.controllers.start', [])
    .controller('StartEttersendingCtrl', function ($scope, ettersendingService, data, EttersendingMetadataResolver) {
        $scope.mineInnsendinger = data.config["minehenvendelser.link.url"];

        $scope.sisteInnsendtBehandling;
        $scope.innsendtDato;


        var fristDato;
        EttersendingMetadataResolver.then(function(result) {
            var antallDager = data.config["soknad.ettersending.antalldager"];
            $scope.innsendtDato = new Date(parseInt(result.innsendtdato));

            $scope.sisteInnsendtBehandling = result.sisteinnsendtbehandling;
            fristDato = new Date();
            fristDato.setDate($scope.innsendtDato.getDate() + parseInt(antallDager));
            console.log("antall dager er: " + antallDager);
            console.log("innsendt dato er: " + $scope.innsendtDato);
            console.log("fristen er: " + fristDato);
        });
        $scope.kanStarteEttersending = function () {
            fristDato.setHours(23);
            fristDato.setMinutes(59);
            fristDato.setSeconds(59);

            var idag = new Date();

            console.log("idag er" + idag);
            console.log("kan starte ettersending?");
            console.log(fristDato > idag);

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

                    var baseUrl = window.location.href.substring(0, window.location.href.indexOf('/sendsoknad'));
                    window.location.href = baseUrl + '/sendsoknad/ettersending/' + behandlingId + '#/vedlegg';
                },
                function () {
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };
    }
);
