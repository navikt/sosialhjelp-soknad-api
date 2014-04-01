angular.module('nav.services.faktum', [])
    .factory('Faktum', ['$resource', function ($resource) {
        var url = '/sendsoknad/rest/soknad/:soknadId/fakta/:faktumId/:mode';
        return $resource(url,
            {soknadId: '@soknadId', faktumId: '@faktumId', mode: '@mode'},
            {
                save  : { method: 'POST', params: {mode: ''}},
                delete: { method: 'POST', params: {mode: 'delete'}}
            }
        );
    }]);