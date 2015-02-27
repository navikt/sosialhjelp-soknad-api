angular.module('nav.land.resolver', [])
    .factory('LandResolver', ['landService', 'data', function (landService, data) {
        var land = landService.get(
            function (result) { // Success
                data.land = result;
            }
        );

        return land.$promise;
    }])
    .factory('EosLandResolver', function (landService, data) {

        var eosLand = landService.getEosland(
            function (result) {
                data.eosLand = result;
            });

        return eosLand.$promise;
    });