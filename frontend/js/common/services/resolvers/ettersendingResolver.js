angular.module('nav.services.resolvers.ettersending', [])
    .factory('EttersendingResolver', ['ettersendingService', 'data', function (ettersendingService, data) {
        var brukerbehandlingsid = getBehandlingIdFromUrl();

        var ettersending = ettersendingService.get({behandlingsId: brukerbehandlingsid},
            function (result) {
                data.soknad = result;
                data.finnFaktum = function (key) {
                    var res = null;
                    data.soknad.faktaListe.forEach(function (item) {
                        if (item.key === key) {
                            res = item;
                        }
                    });
                    return res;
                };
            }
        );

        return ettersending.$promise;
    }]);