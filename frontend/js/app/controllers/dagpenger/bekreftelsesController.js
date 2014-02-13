angular.module('nav.bekreftelse', [])
    .controller('BekreftelsesCtrl', ['$scope', 'data', '$window', '$timeout', function ($scope, data, $window, $timeout) {

        $timeout(function() {
            var mineHenveldelserBaseUrl = data.config["minehenvendelser.link.url"];
            $window.location.href = mineHenveldelserBaseUrl + "?behandlingsId=" + getBehandlingIdFromUrl();

        }, 3000);
    }]);