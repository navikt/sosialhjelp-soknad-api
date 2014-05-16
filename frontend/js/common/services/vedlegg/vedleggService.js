angular.module('nav.services.vedlegg', [])
    .factory('vedleggService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:soknadId/vedlegg/:vedleggId/:action',
            {
                soknadId : '@soknadId',
                vedleggId: '@vedleggId',
                skjemaNummer  : '@skjemaNummer'},
            {
                get   : { method: 'GET', params: {} },
                hentAnnetVedlegg : {
                    url: '/sendsoknad/rest/soknad/:soknadId/vedlegg/:faktumId/hentannetvedlegg',
                    method: 'GET',
                    params: {faktumId: '@faktumId'}},
                merge : { method: 'POST', params: {action: 'generer'} },
                remove: {method: 'POST', params: {action: 'delete'}},
                underbehandling: {method: 'GET', params: {action: 'underBehandling'}, isArray: true }
            }
        );
    }]);