'use strict';

angular.module('app.services', ['ngResource'])

    .config(function ($httpProvider) {
        $httpProvider.responseInterceptors.push('resetTimeoutInterceptor');
    })

    .factory('resetTimeoutInterceptor', function () {
        return function (promise) {
            return promise.then(function (response) {
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

/**
 * Service for å lagre Faktum
 */
    .factory('Faktum', function ($resource) {
        var url = '/sendsoknad/rest/soknad/:soknadId/fakta/:faktumId/:mode';
        return $resource(url,
            {soknadId: '@soknadId', faktumId: '@faktumId', mode: '@mode'},
            {
                save: { method: 'POST', params: {mode:''}},
                delete: { method: 'POST', params: {mode: 'delete'}}
            }
        )
    })

/**
 * Service som behandler vedlegg
 */
    .factory('vedleggService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:soknadId/faktum/:faktumId/vedlegg/:vedleggId/:action',
            {
                soknadId: '@soknadId',
                faktumId: '@faktumId',
                vedleggId: '@vedleggId',
                gosysId: '@gosysId'},
            {
                get: { method: 'GET', params: {} },
                create: { method: 'POST', params: {} },
                merge: { method: 'POST', params: {action: 'generer'} },
                remove: {method: 'POST', params: {action: 'delete'}}
            }
        );
    })
/**
 * Service som behandler vedlegg
 */
    .factory('VedleggForventning', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:soknadId/forventning?rand=' + new Date().getTime(), {
            soknadId: '@faktum.soknadId'
        }, {
            slettVedlegg: {
                url: '/sendsoknad/rest/soknad/:soknadId/faktum/:faktumId/vedlegg/:vedleggId/delete',
                method: 'POST',
                params: {
                    faktumId: '@faktum.faktumId',
                    vedleggId: '@vedlegg.id'
                }
            },
            endreValg: {
                url: '/sendsoknad/rest/soknad/:soknadId/forventning/valg',
                method: 'POST'
            }
        });
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
