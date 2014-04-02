angular.module('nav.services.fortsettsenere', [])
    .factory('fortsettSenereService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/:behandlingId/fortsettsenere',
            {soknadId: '@behandlingId'},
            {send: {method: 'POST'}}
        );
    }]);