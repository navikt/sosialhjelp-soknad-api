angular.module('nav.services.resolvers.ettersendingmetadata', [])
    .factory('EttersendingMetadataResolver', ['$resource', 'data', '$q', '$route', function ($resource, data, $q, $route) {
        var metadata = $q.defer();

        var behandlingId = getBehandlingsIdFromUrlForEttersending();
        $route.current.params.behandlingId = behandlingId;

        $resource('/sendsoknad/rest/soknad/behandlingmetadata/:behandlingId').get(
            {behandlingId: behandlingId},
            function (result) {
                metadata.innsendtDato = result.result;
                metadata.resolve(result);
            });
        return metadata.promise;
    }]);
