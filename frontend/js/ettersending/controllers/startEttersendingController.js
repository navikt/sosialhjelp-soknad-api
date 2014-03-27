angular.module('nav.ettersending')
    .controller('StartEttersendingCtrl', ['$scope', 'ettersendingService', '$location',
        function ($scope, ettersendingService, $location) {
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
                        $location.path('/vedlegg');
                    },
                    function() {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            };
        }
    ]);
