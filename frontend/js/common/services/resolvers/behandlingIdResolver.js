angular.module('nav.services.resolvers.behandlingsid', [])
    .factory('BehandlingIdResolver', ['$resource', '$q', '$route' , function ($resource, $q, $route) {
        var behandlingsIdDefer = $q.defer();

        var behandlingId;
        if (erSoknadStartet()) {
            behandlingId = getBehandlingIdFromUrl();
        } else {
            behandlingId = $route.current.params.behandlingId;
        }

        if (behandlingId) {
            $resource('/sendsoknad/rest/soknad/behandling/:behandlingId').get(
                {behandlingId: behandlingId},
                function (result) {
                    $route.current.params.soknadId = result.result;
                    behandlingsIdDefer.resolve(result.result);
                }
            );
        } else {
            behandlingsIdDefer.resolve();
        }


        return behandlingsIdDefer.promise;
    }]);