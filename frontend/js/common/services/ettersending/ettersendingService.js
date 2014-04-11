angular.module('nav.services.ettersending', [])
    .factory('ettersendingService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/ettersending/:action/:behandlingsId',
            { behandlingsId: '@behandlingsId' },
            {
                create : { method: 'POST' },
                send   : { method: 'POST', params: { action: 'send' }},
                delete : { method: 'POST', params: { action: 'delete' }}
            }
        );
    }]);