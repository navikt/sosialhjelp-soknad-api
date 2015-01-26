angular.module('nav.bekreftelse', [])
    /* Er avhengig av at søknadsoppsett er hentet før vi kommer til denne kontrolleren, da temakode blir hentet sånn
     Henter ikke for bekreftelsessiden siden man bare skal videresendes hit, og siden det blir krøll på backend
     */
    .controller('BekreftelsesCtrl', function ($scope, $window, $timeout, $routeParams, $rootElement, data, bekreftelseEpostService, $location) {
        var appName = $rootElement.attr('data-ng-app');
        
        if(data.fakta === undefined) {
            $location.path("/feilside/soknadikkefunnet");
            return false;
        }

        $scope.epost = data.finnFaktum('epost');
        if(!$scope.epost) {
            $scope.epost = {value: data.finnFaktum('personalia').properties.epost};
        }

        $scope.cmsprefix = {value: appName};
        $scope.sendtEpost = {value: false};
        $scope.fullfort = {value: false};
        $scope.fremdriftsindikator = {laster: false };
        $scope.erEttersendelse = {value: false};

        $scope.sendEpost = function (form) {
            $scope.temaKode = {value: data.soknadOppsett.temaKode};

            if (form.$valid) {
                if($scope.epost.value) {
                    $scope.sendtEpost.value = true;
                }
                $scope.fullfort.value = true;
                $scope.fremdriftsindikator.laster = true;

                new bekreftelseEpostService({epost: $scope.epost.value, temaKode: $scope.temaKode.value, erEttersendelse: $scope.erEttersendelse.value}).$send({behandlingId: $routeParams.behandlingsId}).then(function () {
                    $timeout(function() {
                        var saksoversiktBaseUrl = data.config['saksoversikt.link.url'];

                        redirectTilUrl(saksoversiktBaseUrl + '/detaljer/' + $scope.temaKode.value + '/' + $routeParams.behandlingsId);
                    }, 3000);
                });
            }
        };
    });