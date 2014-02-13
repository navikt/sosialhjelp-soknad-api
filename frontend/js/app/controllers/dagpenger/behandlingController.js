angular.module('nav.behandlingside', [])
    .controller('BehandlingCtrl', ['$routeParams', '$location', function ($routeParams, $location) {
        $location.path('#/soknad');
    }]);