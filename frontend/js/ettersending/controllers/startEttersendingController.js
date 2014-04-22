angular.module('nav.ettersending.controllers.start', [])
    .controller('StartEttersendingCtrl', function ($scope, ettersendingService) {
            $scope.fremdriftsindikator = {
                laster: false
            };

            $scope.startEttersending = function($event) {
                $event.preventDefault();
                var behandlingId = getBehandlingIdFromUrl();
                $scope.fremdriftsindikator.laster = true;
                ettersendingService.create({},
                    {behandlingskjedeId: behandlingId},
                    function() {

                        var baseUrl = window.location.href.substring(0, window.location.href.indexOf('/sendsoknad'));
                        window.location.href = baseUrl + '/sendsoknad/ettersending/' + behandlingId + '#/vedlegg';
                    },
                    function() {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            };
        }
    );
