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
				create : { method: 'POST', params: {param: '@param', action: 'opprett'} },
				send   : {method: 'POST', params: {param: '@param', action: 'send'}},
				remove : {method: 'POST', params: {param: '@param', action: 'delete'}},
				options: {method: 'GET', params: {param: '@param', action: 'options'}},
				behandling: {method: 'GET', params: {param: '@param', action: 'behandling'}}
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
				save  : { method: 'POST', params: {mode: ''}},
				delete: { method: 'POST', params: {mode: 'delete'}}
			}
		)
	})

/**
 * Service som behandler vedlegg
 */
	.factory('vedleggService', function ($resource) {
		return $resource('/sendsoknad/rest/soknad/:soknadId/vedlegg/:vedleggId/:action?rand=' + new Date().getTime(),
			{
				soknadId : '@soknadId',
				vedleggId: '@vedleggId',
				skjemaNummer  : '@skjemaNummer'},
			{
				get   : { method: 'GET', params: {} },
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
	.factory('VedleggForventning', function ($resource) {
		return $resource('/sendsoknad/rest/soknad/:soknadId/:faktumId/forventning?rand=' + new Date().getTime(), {
			soknadId: '@faktum.soknadId'
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

     .factory('oppsummeringService', function ($http, $q) {
		return {
			get: function(soknadId) {
				var deferred = $q.defer();
				$http.get('/sendsoknad/rest/soknad/oppsummering/' + soknadId).then(function(response) {
					deferred.resolve(response.data.substring(1, response.data.length - 1));
				});
				return deferred.promise;
			}
		}
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
