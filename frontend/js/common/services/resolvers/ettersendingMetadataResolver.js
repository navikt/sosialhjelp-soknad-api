angular.module('nav.services.resolvers.ettersendingmetadata', [])
    .factory('EttersendingMetadataResolver', function ($resource, data, $q, $route, $location) {
        var metadata = $q.defer();
        var behandlingId = data.soknad ? data.soknad.behandlingskjedeId : getBehandlingsIdFromUrlForEttersending();
        
        $route.current.params.behandlingId = behandlingId;

        $resource('/sendsoknad/rest/soknad/behandlingmetadata/:behandlingId').get(
            {behandlingId: behandlingId},
            function (result) {
                metadata.innsendtDato = result.result;
                metadata.resolve(result);
            });

        if(data.soknad) {
            $location.path("/" + behandlingId + "/vedlegg");
        }

        return metadata.promise;
    });
