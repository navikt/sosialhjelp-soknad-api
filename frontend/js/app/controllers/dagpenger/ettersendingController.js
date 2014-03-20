angular.module('nav.ettersending', [])
.controller('EttersendingCtrl', ['$scope', '$location', 'data', '$http', function ($scope, $location, data, $http) {
        $scope.vedleggListe = data.soknad.vedlegg;


        $scope.sendEttersending = function() {

            var soknadId = window.location.href.split("/").last();
            var behandlingsId = getBehandlingIdFromUrl();

            $http.post('/sendsoknad/rest/soknad/sendettersending', {behandlingsId: behandlingsId, soknadId: soknadId}).then(function(result) {
                console.log("done");
            });
        }
    }]);
