/* global getIEVersion, TimeoutBox, window*/

(function () {
    'use strict';

    // TODO: Denne modulen må ryddes opp i
    angular.module('ettersending.services', ['ngResource'])

        .config(['$httpProvider', function ($httpProvider) {
            $httpProvider.interceptors.push('resetTimeoutInterceptor');
            $httpProvider.interceptors.push('xsrfRelast');

            if (getIEVersion() < 10) {
                $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
            }
        }])

        // Resetter session-timeout
        .factory('resetTimeoutInterceptor', [function () {
            return {
                'response': function(response) {
                    // Bare reset dersom kallet gikk gjennom
                    TimeoutBox.startTimeout();
                    return response;
                }
            };
        }])

        .factory('xsrfRelast', ['$q', function ($q) {
            return {
                'responseError': function(response){
                    if(response.status === 403){
                        //("Vi må håndtere feil fra rest kall på en god måte. 403 er når xsrf token ikke matcher. Må laste siden på nytt.")
                    }
                    $q.reject(response);
                }
            };
        }])
        // Legger på tilfeldige tall sist i GET-requests for å forhindre caching i IE
        .factory('httpRequestInterceptorPreventCache', [function() {
            return {
                'request': function(config) {
                    if (getIEVersion > 0) {
                        if (config.method === "GET" && config.url.indexOf('.html') < 0) {
                            config.url = config.url + '?rand=' + new Date().getTime();
                        }
                    }

                    return config;
                }
            };
        }])

        /**
         * Service som henter en søknad fra henvendelse
         */
        .factory('ettersendingService', ['$resource', function ($resource) {
            return $resource('/sendsoknad/rest/ettersending/:action/:behandlingsId',
                { behandlingsId: '@behandlingsId' },
                {
                    create : { method: 'POST' },
                    send   : { method: 'POST', params: { behandlingsId: '@behandlingsId', action: 'send' }}
                }
            );
        }])


    /**
     * Service for å lagre Faktum
     */
        .factory('Faktum', ['$resource', function ($resource) {
            var url = '/sendsoknad/rest/soknad/:soknadId/fakta/:faktumId/:mode';
            return $resource(url,
                {soknadId: '@soknadId', faktumId: '@faktumId', mode: '@mode'},
                {
                    save  : { method: 'POST', params: {mode: ''}},
                    delete: { method: 'POST', params: {mode: 'delete'}}
                }
            );
        }])

// TODO: Disse må ryddes opp i
    /**
     * Service som behandler vedlegg
     */
        .factory('vedleggService', ['$resource', function ($resource) {
            return $resource('/sendsoknad/rest/soknad/:soknadId/vedlegg/:vedleggId/:action',
                {
                    soknadId : '@soknadId',
                    vedleggId: '@vedleggId',
                    skjemaNummer  : '@skjemaNummer'},
                {
                    get   : { method: 'GET', params: {} },
                    hentAnnetVedlegg : {
                        url: '/sendsoknad/rest/soknad/:soknadId/vedlegg/:faktumId/hentannetvedlegg?rand=' + new Date().getTime(),
                        method: 'GET',
                        params: {faktumId: '@faktumId'}},
                    create: { method: 'POST', params: {} },
                    merge : { method: 'POST', params: {action: 'generer'} },
                    remove: {method: 'POST', params: {action: 'delete'}},
                    underbehandling: {method: 'GET', params: {action: 'underBehandling'}, isArray: true }
                }
            );
        }])

    /**
     * Service som behandler vedlegg
     */

        // TODO: Disse må ryddes opp i
        .factory('VedleggForventning', ['$resource', function ($resource) {
            return $resource('/sendsoknad/rest/soknad/:soknadId/:faktumId/forventning', {
                soknadId: '@soknadId',
                vedleggId: '@vedleggId'
            }, {
                slettVedlegg: {
                    url   : '/sendsoknad/rest/soknad/:soknadId/faktum/:faktumId/vedlegg/:vedleggId/delete',
                    method: 'POST',
                    params: {
                        faktumId : '@faktum.faktumId',
                        vedleggId: '@vedlegg.vedleggId'
                    }
                }
            });
        }])

        .factory('fortsettSenereService', ['$resource', function ($resource) {
            return $resource('/sendsoknad/rest/soknad/:behandlingId/fortsettsenere',
                {soknadId: '@behandlingId'},
                {send: {method: 'POST'}}
            );
        }])

        .factory('landService', ['$resource', function ($resource) {
            return $resource('/sendsoknad/rest/soknad/kodeverk/landliste');
        }])

        .factory('StartSoknadService', ['data', '$resource', '$q', function (data, $resource, $q) {
            var deferred = $q.defer();
            var soknadType = window.location.pathname.split('/')[3];

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
        }]);
}());