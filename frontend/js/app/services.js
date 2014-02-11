'use strict';
// TODO: Denne modulen må ryddes opp i
angular.module('app.services', ['ngResource'])

	.config(function ($httpProvider) {
		$httpProvider.interceptors.push('resetTimeoutInterceptor');
		$httpProvider.interceptors.push('settDelstegStatusEtterKallMotServer');

        if (getIEVersion() < 10) {
            $httpProvider.interceptors.push('httpRequestInterceptorPreventCache');
        }
	})

    // Resetter session-timeout
	.factory('resetTimeoutInterceptor', function () {
        return {
            'response': function(response) {
                // Bare reset dersom kallet gikk gjennom
                TimeoutBox.startTimeout();
                return response;
            }
        }
	})

    // Oppdaterer delstegstatus dersom man gjør endringer på faktum eller vedlegg
    .factory('settDelstegStatusEtterKallMotServer', ['data', function (data) {
        return {
            'response': function(response) {
                if (response.config.method === 'POST') {
                    if (data.soknad) {
                        data.soknad.sistLagret = new Date().getTime();
                    }
                    var urlArray = response.config.url.split('/');
                    if (urlArray.contains('fakta')) {
                        data.soknad.delstegStatus = 'UTFYLLING';
                    } else if (urlArray.contains('vedlegg')) {
                        data.soknad.delstegStatus = 'SKJEMA_VALIDERT';
                    } else if (urlArray.contains('delsteg')) {
                        if (response.config.data.delsteg === "vedlegg") {
                            data.soknad.delstegStatus = 'SKJEMA_VALIDERT';
                        } else if (response.config.data.delsteg === "oppsummering") {
                            data.soknad.delstegStatus = 'VEDLEGG_VALIDERT';
                        } else {
                            data.soknad.delstegStatus = 'UTFYLLING';
                        }
                    }
                }
                return response;
            }
        }
    }])

    // Legger på tilfeldige tall sist i GET-requests for å forhindre caching i IE
    .factory('httpRequestInterceptorPreventCache', [function() {
        return {
            'request': function(config) {
                if (config.method === "GET" && config.url.indexOf('.html') < 0) {
                    config.url = config.url + '?rand=' + new Date().getTime();
                }
                return config;
            }
        }
    }])

/**
 * Service som henter en søknad fra henvendelse
 */
	.factory('soknadService', function ($resource) {
		return $resource('/sendsoknad/rest/soknad/:action/:soknadId',
            { soknadId: '@soknadId', soknadType: '@soknadType', delsteg: '@delsteg'},
			{
				create : {
                    method: 'POST',
                    params: {soknadType: '@soknadType'},
                    url: '/sendsoknad/rest/soknad/opprett/:soknadType'
                },
				send   : { method: 'POST', params: {soknadId: '@soknadId', action: 'send' }},
				remove : { method: 'POST', params: {soknadId: '@soknadId', action: 'delete' }},
				options: { method: 'GET', params: {soknadId: '@soknadId', action: 'options' }},
				behandling: { method: 'GET', params: {soknadId: '@soknadId', action: 'behandling' }},
				metadata: { method: 'GET', params: {soknadId: '@soknadId', action: 'metadata' }},
				delsteg: {
                    method: 'POST',
                    params: {soknadId: '@soknadId', delsteg: '@delsteg' },
                    url: '/sendsoknad/rest/soknad/delsteg/:soknadId/:delsteg'
                }
			}
		);
	})

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
	.factory('vedleggService', function ($resource) {
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
	})

/**
 * Service som behandler vedlegg
 */

 // TODO: Disse må ryddes opp i
	.factory('VedleggForventning', function ($resource) {
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
			},
			endreValg   : {
				url   : '/sendsoknad/rest/soknad/:soknadId/forventning/valg',
				method: 'POST'
			}
		});
	})

	.factory('fortsettSenereService', function ($resource) {
		return $resource('/sendsoknad/rest/soknad/:behandlingId/fortsettsenere',
			{soknadId: '@behandlingId'},
			{send: {method: 'POST'}}
		);
	})

	// Husk språkstøtte...?
	.factory('tekstService', function ($resource) {
		return $resource('/sendsoknad/rest/enonic/:side',
			{},
			{get: {
				method: 'GET'
			}});
	})

    .factory('landService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/kodeverk/landliste');
    })

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
