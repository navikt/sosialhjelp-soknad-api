angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', 'data', '$location', '$routeParams', 'soknadService', 'personalia', 'oppsummeringService', '$window', function ($scope, data, $location, $routeParams, soknadService, personalia, oppsummeringService, $window) {
        $scope.personalia = personalia;
        $scope.oppsummeringHtml = '';
        $scope.harbekreftet = {value: ''};
        $scope.skalViseFeilmelding = {value: false};

        $scope.soknadId = data.soknad.soknadId;
        oppsummeringService.get($scope.soknadId).then(function (markup) {
            $scope.oppsummeringHtml = markup;
        });

        $scope.$watch(function () {
            if ($scope.harbekreftet) {
                return $scope.harbekreftet.value;
            }
        }, function () {
            $scope.skalViseFeilmelding.value = false;
        })

        $scope.sendSoknad = function () {
            if ($scope.harbekreftet.value) {
                $scope.skalViseFeilmelding.value = false;

                soknadService.send({param: $scope.soknadId, action: 'send'},
                    //Success
                    function () {
                        $location.path('bekreftelse');
                    },
                    //Error
                    function () {
                        //TODOD
                    }
                );
            } else {
                $scope.skalViseFeilmelding.value = true;
            }
        }
    }])
    .filter('formatterFnr', function () {
        return function (fnr) {
            return fnr.substring(0, 6) + " " + fnr.substring(6, fnr.length);
        };
    });