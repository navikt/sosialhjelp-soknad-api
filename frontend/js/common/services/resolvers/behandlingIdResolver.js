angular.module('nav.services.resolvers.behandlingsid', [])
    .factory('BehandlingIdResolver', function ($resource, $q, $route) {
        var behandlingsIdDefer = $q.defer();

        var behandlingId;
        if (erSoknadStartet() || erEttersending()) {
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
                },
                function () {
                    redirectTilUrl("/sendsoknad/avbrutt");
                    behandlingsIdDefer.reject("Fant ikke søknad for behandlingsID");
                }
            );
        } else {
            behandlingsIdDefer.reject("Fant ikke behandlingsID");
        }

        return behandlingsIdDefer.promise;
    });