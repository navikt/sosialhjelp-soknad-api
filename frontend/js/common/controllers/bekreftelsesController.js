angular.module('nav.bekreftelse', [])
    /* Er avhengig av at søknadsoppsett er hentet før vi kommer til denne kontrolleren, da temakode blir hentet sånn
     Henter ikke for bekreftelsessiden siden man bare skal videresendes hit, og siden det blir krøll på backend
     */
    .controller('BekreftelsesCtrl', function ($scope, config, $window, $timeout, $routeParams, $rootElement, data, bekreftelseEpostService) {
        var appName = $rootElement.attr('data-ng-app');
        $scope.cmsprefix = {
            value: appName
        };

        $scope.epost = {
            value: data.finnFaktum('epost')
        };

        $scope.brukerBehandlingId = data.soknad.brukerBehandlingId;

        if (!$scope.epost.value) {
            $scope.epost.value = data.finnFaktum('personalia').properties.epost;
        }

        $scope.sendEpost = function (form) {
            if (form.$valid) {
                new bekreftelseEpostService().$send({behandlingId: $scope.brukerBehandlingId, bekreftelsesepost: $scope.epost.value}).then(function (data) {
                       console.log("epost sendt");
//                    var saksoversiktBaseUrl = config['saksoversikt.link.url'];
//                    $window.location.href = saksoversiktBaseUrl + '/detaljer/' + data.soknadOppsett.temaKode + '/' + $routeParams.behandlingsId;
                });
            }
        };
    });