angular.module('nav.services.soknad', [])
    .factory('soknadService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:action/:soknadId',
            { soknadId: '@soknadId', soknadType: '@soknadType', delsteg: '@delsteg'},
            {
                create : {
                    method: 'POST',
                    url: '/sendsoknad/rest/soknad/opprett'
                },
                send   : { method: 'POST', params: {soknadId: '@soknadId', action: 'send' }},
                remove : { method: 'POST', params: {soknadId: '@soknadId', action: 'delete' }},
                options: { method: 'GET', params: {soknadId: '@soknadId', action: 'options' }},
                behandling: { method: 'GET', params: {soknadId: '@soknadId', action: 'behandling' }},
                metadata: { method: 'GET', params: {soknadId: '@soknadId', action: 'metadata' }},
                opprettEttersending: { method: 'POST', params: {action: 'ettersending'}},
                delsteg: {
                    method: 'POST',
                    params: {soknadId: '@soknadId', delsteg: '@delsteg' },
                    url: '/sendsoknad/rest/soknad/delsteg/:soknadId/:delsteg'
                }
            }
        );
    }]);