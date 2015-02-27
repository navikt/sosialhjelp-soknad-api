angular.module('ettersending.bekreftelse', [])
    /* Er avhengig av at søknadsoppsett er hentet før vi kommer til denne kontrolleren, da temakode blir hentet sånn
     Henter ikke for bekreftelsessiden siden man bare skal videresendes hit, og siden det blir krøll på backend
     */
    .controller('BekreftelseEttersendingCtrl', function ($scope, $window, $timeout, $rootElement, data, bekreftelseEpostService) {
        var appName = $rootElement.attr('data-ng-app');
        $scope.cmsprefix = {value: appName};

        $scope.epost = data.finnFaktum('epost');
        if(!$scope.epost) {
            $scope.epost = { value: data.finnFaktum('personalia').properties.epost };
        }

        $scope.erEttersendelse = {value: true};
        $scope.fremdriftsindikator = {laster: true};
        $scope.temaKode = {value: data.soknadOppsett.temaKode};
        var brukerBehandlingId = data.soknad.brukerBehandlingId;

        new bekreftelseEpostService({epost: $scope.epost.value, temaKode: $scope.temaKode.value, erEttersendelse: $scope.erEttersendelse.value}).$send({behandlingId: brukerBehandlingId}).then(function () {
            $timeout(function() {
                var saksoversiktBaseUrl = data.config['saksoversikt.link.url'];
                redirectTilUrl(saksoversiktBaseUrl + '/detaljer/' + $scope.temaKode.value + '/' + brukerBehandlingId);
            }, 3000);
        });
    });