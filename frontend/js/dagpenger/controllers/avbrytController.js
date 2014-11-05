angular.module('nav.avbryt', [])
    .controller('AvbrytCtrl', ['$scope', 'soknadService', 'data', function ($scope, soknadService, data) {
        $scope.fremdriftsindikator = {
            laster: false

        };
        $scope.brukerBehandlingId = data.soknad.brukerBehandlingId;

        $scope.krevBekreftelse = data.fakta.filter(function(item) {
            return item.type==="BRUKERREGISTRERT" && (item.value !== null || Object.keys(item.properties).length > 0);
        }).length > 1;

        $scope.submitForm = function () {
            var start = $.now();
            $scope.fremdriftsindikator.laster = true;

            soknadService.remove({soknadId: data.soknad.soknadId},
                function () { // Success
                    var delay = 1500 - ($.now() - start);
                    setTimeout(function () {
                        $scope.$apply(function () {
                            var baseUrl = window.location.href.substring(0, window.location.href.indexOf('/sendsoknad'));
                            window.location.href = baseUrl + '/sendsoknad/avbrutt';
                        });
                    }, delay);
                },
                function () { // Error
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };

        if (!$scope.krevBekreftelse) {
            $scope.submitForm();
        }
    }]);
