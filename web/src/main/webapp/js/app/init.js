angular.module('sendsoknad')
    .value('data', {})
    .factory('TekstService', ['data', '$resource', '$q', '$route', function(data, $resource, $q, $route) {
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function(result) { // Success
                data.tekster = result;
            }
        );
        promiseArray.push(tekster.$promise);

        var d = $q.all(promiseArray);

        return d;
    }])
    .factory('HentSoknadService', ['data', '$resource', '$q', '$route', function(data, $resource, $q, $route) {
        var soknadId = $route.current.params.soknadId;
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function(result) { // Success
                data.tekster = result;
            }
        );
        promiseArray.push(tekster.$promise);

        if (soknadId != undefined) {
            var soknad = $resource('/sendsoknad/rest/soknad/' + soknadId).get(
                function(result) { // Success
                    data.soknad = result;
                }
            );
            promiseArray.push(soknad.$promise);
        }

        var d = $q.all(promiseArray);

        return d;
    }]);