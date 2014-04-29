angular.module('nav.services.resolvers.ettersendingmetadata', [])
    .factory('EttersendingMetadataResolver', ['$resource', 'data', '$q', '$route', function ($resource, data, $q, $route) {
        var metadata = $q.defer();

        var url = window.location.href;
        // Hack for å hente ut behandlingID
        var behandlingId = url.substring(url.indexOf("startettersending/") + 18, url.indexOf("#"));
        $route.current.params.behandlingId = behandlingId;

        $resource('/sendsoknad/rest/soknad/behandlingmetadata/:behandlingId').get(
            {behandlingId: behandlingId},
            function (result) {
                metadata.innsendtDato = result.result;
                metadata.resolve(result);
            });
        return metadata.promise;
    }]);
