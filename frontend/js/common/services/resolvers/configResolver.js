angular.module('nav.services.resolvers.config', [])
    .factory('ConfigResolver', ['$resource', 'data', function ($resource, data) {
        var config = $resource('/sendsoknad/rest/getConfig').get(
            function (result) {
                data.config = result;
            }
        );

        return config.$promise;
    }])
    .factory('ConfigForSoknadResolver', ['$resource', 'data', '$q', 'BehandlingIdResolver', function ($resource, data, $q, BehandlingIdResolver) {
        var configDefer = $q.defer();
        BehandlingIdResolver
            .then(function(result) {
                var soknadId = result;
                $resource('/sendsoknad/rest/getConfig/' + soknadId).get(
                    function (result) {
                        data.config = result;
                        configDefer.resolve();
                    }
                );
            })
            .catch(function() {
                // TODO: Håndtere dersom man ikke kunne hente søknadsid
                configDefer.reject("Kunne ikke hente søknadsId for behandlingsId");
            });

        return configDefer;
    }]);