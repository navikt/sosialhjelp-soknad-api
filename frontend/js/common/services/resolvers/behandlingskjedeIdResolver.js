angular.module('nav.services.resolvers.behandlingskjedeid', [])
    .factory('BehandlingskjedeIdResolver', function ($resource, $q) {
        var behandlingsIdDefer = $q.defer();

        var behandlingId = getBehandlingIdFromUrl();
        if (behandlingId) {
            $resource('/sendsoknad/rest/soknad/behandlingskjede/:behandlingskjedeId').get(
                {behandlingskjedeId: behandlingId},
                function (result) {
                    behandlingsIdDefer.resolve(result.result);
                }
            );
        } else {
            behandlingsIdDefer.reject("Fant ikke behandlingsID i URL");
        }

        return behandlingsIdDefer.promise;
    });