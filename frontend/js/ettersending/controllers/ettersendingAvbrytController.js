angular.module('nav.ettersending.controllers.avbryt', [])
    .controller('EttersendingAvbrytCtrl', ['$scope', 'data', 'ettersendingService', '$timeout', function ($scope, data, ettersendingService, $timeout) {
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.slettEttersending = function() {
            $scope.fremdriftsindikator.laster = true;
            var start = $.now();

            ettersendingService.delete({},
                {soknadId: data.soknad.soknadId},
                function() {
                    var delay = 1500 - ($.now() - start);
                    $timeout(function() {
                        var baseUrl = window.location.href.substring(0, window.location.href.indexOf('/sendsoknad'));
                        window.location.href = baseUrl + '/sendsoknad/ettersending/avbrutt';
                    }, delay);
                },
                function() {
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };
    }]);
