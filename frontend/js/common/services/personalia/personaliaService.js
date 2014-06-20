angular.module('nav.services.personalia', [])
    .factory('Personalia', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/personalia/:soknadId',
            { soknadId: '@soknadId' },
            {
                lagre   : { method: 'POST' }
            }
        );
    });