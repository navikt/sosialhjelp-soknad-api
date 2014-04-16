angular.module('nav.services.resolvers.soknad', [])
    .factory('SoknadResolver', function (data, $q, soknadService, $http, PersonaliaResolver, BehandlingIdResolver) {
        var soknadId;
        BehandlingIdResolver.then(function(result) {
            soknadId = result;
        });

        var soknadDeferer = $q.defer();
        PersonaliaResolver
            .then(function() {
                soknadService.get({soknadId: soknadId},
                    function (result) { // Success
                        data.soknad = result;
                        soknadDeferer.resolve();
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                soknadDeferer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return soknadDeferer.promise;
    });