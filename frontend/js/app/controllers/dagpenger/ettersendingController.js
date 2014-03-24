angular.module('nav.ettersending', [])
.controller('EttersendingCtrl', ['$scope', '$location', 'data', function ($scope, $location, data) {
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

        $scope.erLastetOpp = function(v) {
            return v.innsendingsvalg === 'LastetOpp';
        };

        $scope.erIkkeLastetOpp = function(v) {
            return !$scope.erLastetOpp(v);
        };

        function skalEttersendes(v) {
            return v.opprinneligInnsendingsvalg === 'SendesSenere';
        }

        function skalIkkeEttersendes(v) {
            return !skalEttersendes(v);
        }
        
        $scope.sendEttersending = function() {

            var soknadId = window.location.href.split("/").last();
            var behandlingsId = getBehandlingIdFromUrl();

            $http.post('/sendsoknad/rest/soknad/sendettersending', {behandlingsId: behandlingsId, soknadId: soknadId}).then(function(result) {
                console.log("done");
            });
        }
    }]);
