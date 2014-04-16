angular.module('nav.bekreftelse', [])
    .controller('BekreftelsesCtrl', function ($scope, config, $window, $timeout, $routeParams) {
        $timeout(function() {
            var mineHenveldelserBaseUrl = config["minehenvendelser.link.url"];
            $window.location.href = mineHenveldelserBaseUrl + "?behandlingsId=" + $routeParams.behandlingsId;
        }, 3000);
    });