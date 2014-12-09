angular.module('nav.services.resolvers.vedlegg', [])
    .factory('VedleggResolver', function (vedleggService, BehandlingIdResolver, $q) {
        var vedleggDefer = $q.defer();
        BehandlingIdResolver
            .then(function(result) {
                vedleggService.query({soknadId: result},
                    function (result) {
                        vedleggDefer.resolve(result);
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                vedleggDefer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return vedleggDefer.promise;
    });