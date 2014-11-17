angular.module('nav.services.bekreftelse', [])
    .factory('bekreftelseEpostService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/bekreftelse/:behandlingId/:bekreftelsesepost',
            {soknadId: '@behandlingId'},
            {send: {method: 'POST'}}
        );
    }]);