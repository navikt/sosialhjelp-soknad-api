angular.module('nav.services.resolvers.soknadutslagskriterier', [])
    .factory('SoknadForUtslagskriterierResolver', function (data, $q, $resource, soknadService) {
        var behandlingsIdDefer = $q.defer();
        var behandlingsId = getBehandlingIdFromUrl();

        var soknad = {};
        data.soknad = soknad;

        if(isNotNullOrUndefined(behandlingsId)) {
            $resource('/sendsoknad/rest/soknad/behandling/:behandlingsId').get(
                {behandlingsId: behandlingsId},
                function (result) {
                    soknadService.get({soknadId: result.result},
                        function (result) { // Success
                            data.soknad = result;
                            behandlingsIdDefer.resolve(data.soknad);
                        },
                        function () {
                            redirectTilUrl("#/feilside/soknadikkefunnet");
                            behandlingsIdDefer.reject("Fant ikke søknad.");
                        }
                    );
                },
                function () {
                    behandlingsIdDefer.resolve(soknad);
                }
            );
        } else {
            behandlingsIdDefer.resolve(soknad);
        }
        return behandlingsIdDefer.promise;
    });