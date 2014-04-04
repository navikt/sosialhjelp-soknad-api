angular.module('nav.services.resolvers.utslagskriterier', [])
    .factory('UtslagskriterierResolver', ['$resource', 'data', function ($resource, data) {
        var utslagskriterier = $resource('/sendsoknad/rest/utslagskriterier/').get(
            function (result) {
                data.utslagskriterier = result;
            }
        );

        return utslagskriterier.$promise;
    }]);