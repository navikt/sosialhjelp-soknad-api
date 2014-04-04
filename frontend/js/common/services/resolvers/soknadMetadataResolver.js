angular.module('nav.services.resolvers.soknadmetadata', [])
    .factory('SoknadMetadataResolver', ['$resource', 'data', '$q', 'BehandlingIdResolver', function ($resource, data, $q, BehandlingIdResolver) {
        var metadata = $q.defer();
        if(erSoknadStartet()) {
            BehandlingIdResolver
                .then(function(result) {
                    var soknadId = result;
                    $resource('/sendsoknad/rest/soknad/metadata/:soknadId').get(
                        {soknadId: soknadId},
                        function (result) { // Success
                            data.soknad = result;
                            metadata.resolve();
                        }
                    );
                })
                .catch(function() {
                    // TODO: Håndtere dersom man ikke kunne hente søknadsid
                    metadata.reject("Kunne ikke hente søknadsId for behandlingsId");
                });
        } else {
            metadata.resolve();
        }

        return metadata;
    }]);
