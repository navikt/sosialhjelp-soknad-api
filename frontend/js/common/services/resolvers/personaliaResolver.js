angular.module('nav.services.resolvers.personalia', [])
    .factory('PersonaliaResolver', function ($q, BehandlingIdResolver, Personalia) {

        var personaliaDeferer = $q.defer();
        BehandlingIdResolver
            .then(function(result) {
                var soknadId = result;
                Personalia.lagre({soknadId: soknadId},
                    true,
                    function(result) {
                        personaliaDeferer.resolve(result);
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                personaliaDeferer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return personaliaDeferer.promise;
    })
    .factory('EttersendingPersonaliaResolver', function ($q, BehandlingskjedeIdResolver, Personalia) {

        var personaliaDeferer = $q.defer();
        BehandlingskjedeIdResolver
            .then(function(result) {
                var soknadId = result;
                Personalia.lagre({soknadId: soknadId},
                    '',
                    function(result) {
                        personaliaDeferer.resolve(result);
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                personaliaDeferer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return personaliaDeferer.promise;
    });
