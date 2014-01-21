angular.module('app.brukerdata', ['app.services'])
    .controller('SendSoknadCtrl', function ($scope, $location, $routeParams, soknadService) {
        $scope.sendSoknad = function () {
            soknadService.send({param: $routeParams.soknadId, action: 'send'});
            $location.path('kvittering');
        }
    })
    .controller('SoknadDataCtrl', ['$scope', 'data', '$http', function ($scope, data, $http) {
        $scope.soknadData = data.soknad;
    }])
    .controller('ModusCtrl', function ($scope) {
        $scope.data = {
            redigeringsModus: true
        };

        // TODO: Endre navn. Setter bare til redigerings-/oppsummerings-modus. Trenger vi denne?
        $scope.validateForm = function (invalid) {
            $scope.data.redigeringsModus = invalid;
        }

        $scope.gaTilRedigeringsmodus = function () {
            $scope.data.redigeringsModus = true;
            $scope.$broadcast("ENDRET_TIL_REDIGERINGS_MODUS", {key: 'redigeringsmodus', value: true});
        }

        $scope.hvisIRedigeringsmodus = function () {
            return $scope.data.redigeringsModus;
        }

        $scope.hvisIOppsummeringsmodus = function () {
            return !$scope.hvisIRedigeringsmodus();
        }
    });
