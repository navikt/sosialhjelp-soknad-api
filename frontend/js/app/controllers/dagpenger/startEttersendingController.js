angular.module('nav.ettersending')
    .controller('StartEttersendingCtrl', ['$scope', 'soknadService', '$location',
        function ($scope, soknadService, $location) {
            $scope.fremdriftsindikator = {
                laster: false
            };

            $scope.startEttersending = function($event) {
                $event.preventDefault();
                var behandlingId = getBehandlingIdFromUrl();
                $scope.fremdriftsindikator.laster = true;
                soknadService.opprettEttersending({},
                    {behandlingskjedeId: behandlingId},
                    function(result) {
                        $location.path('/ettersending/' + result.soknadId);
                    },
                    function() {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            };
        }
    ]);
