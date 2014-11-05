angular.module('nav.behandlingside', [])
    .controller('BehandlingCtrl', ['$location', function ($location) {
        $location.path('#/soknad');
    }]);