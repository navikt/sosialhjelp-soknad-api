angular.module('nav.avbryt', [])
    .controller('AvbrytCtrl', ['$scope', '$location', 'soknadService', 'data', function ($scope, $location, soknadService, data) {
        $scope.fremdriftsindikator = {
            laster: false

        }

        $scope.krevBekreftelse = data.fakta.filter(function(item) {
            return item.type==="BRUKERREGISTRERT";
        }).length>0;
        


        $scope.submitForm = function () {
            var start = $.now();
            $scope.fremdriftsindikator.laster = true;

            soknadService.remove({soknadId: data.soknad.soknadId},
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

        if (!$scope.krevBekreftelse) {
            $scope.submitForm();
        }
    }])
    .controller('SlettetCtrl', ['$scope', '$location', 'data', function ($scope, $location, data) {
        $scope.skjemaVeilederUrl = data.config["soknad.skjemaveileder.url"];  
        $scope.mineHenveldelserBaseUrl = data.config["minehenvendelser.link.url"];     
    }]);
