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
        
        $scope.sendEttersending = function() {

            var behandlingsId = getBehandlingIdFromUrl();
            ettersendingService.send({behandlingsId: behandlingsId},
                function(result) {
                    console.log("done");
                }
            );
        }
    }])
    .controller('EttersendingOpplastingCtrl', ['$scope', 'data', function($scope, data) {
        $scope.soknadId = data.soknad.soknadId;
    }]);
