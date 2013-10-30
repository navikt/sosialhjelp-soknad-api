angular.module('sendsoknad')
    .run(['$http', '$templateCache', function($http, $templateCache) {
        $http.get('../html/templates/reell-arbeidssoker.html', {cache: $templateCache});
        $http.get('../html/templates/egen-naering.html', {cache: $templateCache});
        $http.get('../html/templates/verneplikt.html', {cache: $templateCache});
        $http.get('../html/templates/personalia.html', {cache: $templateCache});
        $http.get('../html/templates/arbeidsforhold.html', {cache: $templateCache});
        $http.get('../html/templates/ytelser.html', {cache: $templateCache});
        $http.get('../html/templates/utdanning.html', {cache: $templateCache});
    }])
    .value('data', {})
    .value('basepath', '../')
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