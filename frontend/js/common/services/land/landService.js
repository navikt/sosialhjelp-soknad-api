angular.module('nav.services.land', [])
    .factory('landService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/soknad/kodeverk/landliste');
    }]);