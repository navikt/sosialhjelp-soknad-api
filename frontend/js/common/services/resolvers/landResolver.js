angular.module('nav.services.resolvers.land', [])
    .factory('LandResolver', ['landService', 'data', function (landService, data) {
        var land = landService.get(
            function (result) { // Success
                data.land = result;
            }
        );

        return land.$promise;
    }]);