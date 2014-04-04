angular.module('nav.services.resolvers.soknadoppsett', [])
    .factory('SoknadOppsettResolver', ['data', '$q', 'soknadService', 'BehandlingIdResolver', function (data, $q, soknadService, BehandlingIdResolver) {
        var soknadOppsettDefer = $q.defer();

        BehandlingIdResolver
            .then(function(result) {
                var soknadId = result;
                soknadService.options({soknadId: soknadId},
                    function (result) { // Success
                        data.soknadOppsett = result;
                        soknadOppsettDefer.resolve();
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                soknadOppsettDefer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return soknadOppsettDefer.promise;
    }]);