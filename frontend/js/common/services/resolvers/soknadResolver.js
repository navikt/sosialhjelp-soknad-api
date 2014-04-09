angular.module('nav.services.resolvers.soknad', [])
    .factory('SoknadResolver', ['$rootScope', 'data', 'cms', '$resource', '$q', 'soknadService', 'landService', 'Faktum', '$http', 'BehandlingIdResolver', function ($rootScope, data, cms, $resource, $q, soknadService, landService, Faktum, $http, BehandlingIdResolver) {

        var soknadDeferer = $q.defer();
        BehandlingIdResolver
            .then(function(result) {
                var soknadId = result;
                $http.post('/sendsoknad/rest/soknad/personalia', soknadId).then(function() {
                    soknadService.get({soknadId: soknadId},
                        function (result) { // Success
                            data.soknad = result;
                            soknadDeferer.resolve();
                        }
                    );
                });
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                soknadDeferer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return soknadDeferer.promise;
    }]);