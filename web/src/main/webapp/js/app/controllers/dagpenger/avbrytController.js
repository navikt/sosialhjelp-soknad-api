angular.module('nav.avbryt', [])
    .controller('AvbrytCtrl', ['$scope', 'data', '$location', 'soknadService', function ($scope, data, $location, soknadService) {
        $scope.fremdriftsindikator = {
            laster: false
        };
        $scope.krevBekreftelse = {value: false};
        soknadService.get({param: data.soknad.soknadId}).$promise.then(function (result) {
            var fakta = $.map(result.fakta, function (element) {
                return element.type;
            });

            $scope.krevBekreftelse.value = fakta.indexOf("BRUKERREGISTRERT") >= 0;
            
            if (!$scope.krevBekreftelse.value) {
                $scope.submitForm();
            }
        })

        $scope.submitForm = function () {
            var start = $.now();
            $scope.fremdriftsindikator.laster = true;
            soknadService.remove({param: data.soknad.soknadId},
                function () { // Success
                    var delay = 1500 - ($.now() - start);
                    setTimeout(function () {
                        $scope.$apply(function () {
                            $location.path('slettet');
                        });
                    }, delay);
                },
                function () { // Error
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };
    }]);
