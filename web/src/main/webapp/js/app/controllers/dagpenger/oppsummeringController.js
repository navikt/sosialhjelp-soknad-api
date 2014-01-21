angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', '$location', '$routeParams', 'soknadService', '$http', function ($scope, $location, $routeParams, soknadService, $http) {
        $scope.oppsummeringHtml = '';
        $scope.harbekreftet = {value: ''};
        $scope.skalViseFeilmelding = {value: false};

        $scope.soknadId = $routeParams.soknadId;
        $http.get('/sendsoknad/rest/soknad/oppsummering/' + $scope.soknadId).then(function(response) {
            var soknadElement = $(response.data).filter("#soknad");
            soknadElement.find('.logo').remove();
            soknadElement.find('.hode h1').addClass('stor strek-ikon-soknader');
            soknadElement.find('hr').remove();
            $scope.oppsummeringHtml = soknadElement.html();
        });

        console.log($scope.skalViseFeilmelding.value)

        $scope.$watch(function () {
            if($scope.harbekreftet) {
                return $scope.harbekreftet.value;
            }
        }, function () {
            $scope.skalViseFeilmelding.value = false;
            console.log($scope.skalViseFeilmelding.value)
        });

        $scope.sendSoknad = function () {
            if ($scope.harbekreftet.value) {
                console.log("HEI");
                $scope.skalViseFeilmelding.value = false;
            } else {
                $scope.skalViseFeilmelding.value = true;
            }
            console.log($scope.skalViseFeilmelding.value)

//            soknadService.send({param: $scope.soknadId, action: 'send'});
//            $location.path('kvittering');
        }
    }])
    .filter('formatterFnr', function() {
        return function(fnr) {
            return fnr.substring(0, 6) + " " + fnr.substring(6, fnr.length);
        };
    });