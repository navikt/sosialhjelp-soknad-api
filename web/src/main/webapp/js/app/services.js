'use strict';

angular.module('app.services', ['ngResource'])

    .config(function($httpProvider) {
        $httpProvider.responseInterceptors.push('resetTimeoutInterceptor');
    })

    .factory('resetTimeoutInterceptor', function() {
        return function(promise) {
            return promise.then(function(response) {
                // Bare reset dersom kallet gikk gjennom
                TimeoutBox.startTimeout();
                return response;
            });
        }
    })

    /**
     * Service som henter en søknad fra henvendelse
     */
    .factory('soknadService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:action/:param?rand=' + new Date().getTime(),
            {param: '@param'},
            {
                create: { method: 'POST', params: {param: '@param', action: 'opprett'} },
                send: {method: 'POST', params: {param: '@param', action: 'send'}},
                remove: {method: 'POST', params: {param: '@param', action: 'delete'}},
                options: {method: 'GET', params: {param: '@param', action: 'options'}}
            }
        );
    })

    .factory('forsettSenereService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:soknadId/fortsettsenere',
          {soknadId: '@param'},
            {
                send: {method: 'POST'}
            }
        );
    })

    // Husk språkstøtte...?
    .factory('tekstService', function ($resource) {
        return $resource('/sendsoknad/rest/enonic/:side',
            {},
            {
                get: {
                    method: 'GET'
                }
            });
    })

    .factory('tpsService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:soknadId/personalia');
    })

    .factory('landService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/kodeverk/landliste');
    })


    .factory('StartSoknadService', ['data', '$resource', '$q', function (data, $resource, $q) {
        var deferred = $q.defer();
        var soknadType = window.location.pathname.split("/")[3];

        $resource('/sendsoknad/rest/soknad/opprett/' + soknadType).get(
            function (result) { // Success
                data.soknad = result;
                deferred.resolve(result);
            },
            function () { // Error
                deferred.reject('Klarte ikke laste tekster');
            }
        );
        return deferred.promise;
    }])
