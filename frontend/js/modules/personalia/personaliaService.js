angular.module('nav.personalia.service', [])
    .factory('Personalia', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/personalia/:soknadId',
            { soknadId: '@soknadId' },
            {
                lagre   : { method: 'POST' }
            }
        );
    });