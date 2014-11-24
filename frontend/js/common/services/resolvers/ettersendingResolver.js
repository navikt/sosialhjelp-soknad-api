angular.module('nav.services.resolvers.ettersending', [])
    .factory('EttersendingResolver', function (ettersendingService, data, $q, EttersendingPersonaliaResolver, $route) {
        var ettersendingDefer = $q.defer();
            EttersendingPersonaliaResolver.then(function() {
                var soknadId = $route.current.params.soknadId;
                ettersendingService.get({soknadId: soknadId},
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