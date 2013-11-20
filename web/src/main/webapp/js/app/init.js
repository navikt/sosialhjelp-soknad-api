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
    .factory('InformasjonsSideResolver', ['data', '$resource', '$q', '$route', function(data, $resource, $q, $route) {
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function(result) { // Success
                data.tekster = result;
            }
        );

        var utslagskriterier =  $resource('/sendsoknad/rest/utslagskriterier/').get(
            function(result) {
                data.utslagskriterier = result;
            }
        );
    
        promiseArray.push(tekster.$promise);
        promiseArray.push(utslagskriterier.$promise);

        var d = $q.all(promiseArray);

        return d;
    }])
    .factory('HentSoknadService', ['data', '$resource', '$q', '$route', 'soknadService', function(data, $resource, $q, $route, soknadService) {
        var soknadId = $route.current.params.soknadId;
        var promiseArray = [];

        var tekster = $resource('/sendsoknad/rest/enonic/Dagpenger').get(
            function(result) { // Success
                data.tekster = result;
            }
        );
        promiseArray.push(tekster.$promise);

        var alder = $resource('/sendsoknad/rest/soknad/personalder').get(
            function(result) { // Success
                data.alder = result;
            }
        );
        promiseArray.push(alder.$promise); 

        if (soknadId != undefined) {
            var soknad = soknadService.get({param: soknadId},
                function(result) { // Success
                    data.soknad = result;
                }
            );
            var soknadOppsett = soknadService.options({param: soknadId},
                function(result) { // Success
                    data.soknadOppsett = result;
                });
            promiseArray.push(soknad.$promise, soknadOppsett.$promise);
        }

        var d = $q.all(promiseArray);

        return d;
    }]);