angular.module('nav.bekreftelse', [])
    .controller('BekreftelsesCtrl', function ($scope, config, $window, $timeout, $routeParams, $rootElement) {
        var appName = $rootElement.attr('data-ng-app');
        $scope.tekst = {
            tittelKey: 'dagpenger.bekreftelse',
            informasjonsKey: 'dagpenger.bekreftelse.informasjon'
        };
        if (appName === 'ettersending') {
            $scope.tekst.tittelKey = 'ettersending.bekreftelse';
            $scope.tekst.informasjonsKey = 'ettersending.bekreftelse.informasjon';
        }

        $timeout(function() {
            var mineHenveldelserBaseUrl = config['minehenvendelser.link.url'];
            $window.location.href = mineHenveldelserBaseUrl + '?behandlingsId=' + $routeParams.behandlingsId;
        }, 5000);
    });