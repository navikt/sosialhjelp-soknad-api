angular.module('nav.services.resolvers.ettersending', [])
    .factory('EttersendingResolver', function (ettersendingService, data, BehandlingskjedeIdResolver, $q) {
        var ettersendingDefer = $q.defer();
        BehandlingskjedeIdResolver
            .then(function(result) {
                ettersendingService.get({soknadId: result},
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

                        ettersendingDefer.resolve();
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                ettersendingDefer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return ettersendingDefer.promise;
    });