angular.module('nav.services.ettersending', [])
    .factory('ettersendingService', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:action/:soknadId',
            { soknadId: '@soknadId' },
            {
                create : {
                    method: 'POST',
                    url: '/sendsoknad/rest/soknad/opprett/ettersending/:behandlingskjedeId',
                    params : {behandlingskjedeId: '@behandlingskjedeId'}
                },
                send   : { method: 'POST', params: { action: 'send' }},
                delete : { method: 'POST', params: { action: 'delete' }}
            }
        );
    });