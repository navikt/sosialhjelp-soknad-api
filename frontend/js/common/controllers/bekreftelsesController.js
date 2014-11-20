angular.module('nav.bekreftelse', [])
    /* Er avhengig av at søknadsoppsett er hentet før vi kommer til denne kontrolleren, da temakode blir hentet sånn
     Henter ikke for bekreftelsessiden siden man bare skal videresendes hit, og siden det blir krøll på backend
     */
    .controller('BekreftelsesCtrl', function ($scope, $window, $timeout, $routeParams, $rootElement, data, bekreftelseEpostService) {
        var appName = $rootElement.attr('data-ng-app');

        $scope.cmsprefix = {value: appName};
        $scope.epost = {value: data.finnFaktum('epost')};
        $scope.sendtEpost = {value: false};
        $scope.fullfort = {value: false};
        $scope.fremdriftsindikator = {laster: false };
        $scope.brukerBehandlingId = {value: data.soknad.brukerBehandlingId};
        $scope.erEttersendelse = {value: erEttersending()};

        if (!$scope.epost.value) {
            $scope.epost.value = data.finnFaktum('personalia').properties.epost;
        }

        $scope.sendEpost = function (form) {
            $scope.temaKode = {value: data.soknadOppsett.temaKode};

            if (form.$valid) {
                if($scope.epost.value) {
                    $scope.sendtEpost.value = true;
                }
                $scope.fullfort.value = true;
                $scope.fremdriftsindikator.laster = true;

                new bekreftelseEpostService({epost: $scope.epost.value, temaKode: $scope.temaKode.value, erEttersendelse: $scope.erEttersendelse.value}).$send({behandlingId: $scope.brukerBehandlingId.value}).then(function () {
                    $timeout(function() {
                        var saksoversiktBaseUrl = data.config['saksoversikt.link.url'];

                        redirectTilUrl(saksoversiktBaseUrl + '/detaljer/' + $scope.temaKode.value + '/' + $routeParams.behandlingsId);
                    }, 3000);
                });
            }
        };
    });