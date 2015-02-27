angular.module('nav.land.service', [])
    .factory('landService', ['$resource', function ($resource) {
        return $resource('/sendsoknad/rest/land',
            {},
            {
                getEosland: {
                    method: 'GET',
                    url: '/sendsoknad/rest/land/eos',
                    isArray: true
                }
            }
        );
    }]);