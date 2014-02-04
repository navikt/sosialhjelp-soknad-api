angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', 'data', '$location', '$routeParams', 'soknadService', '$http', '$window', function ($scope, data, $location, $routeParams, soknadService, $http, $window) {
        if (data.soknad.delstegStatus !== 'VEDLEGG_VALIDERT' && data.soknad.delstegStatus !== 'SAMTYKKET') {
            if (data.soknad.delstegStatus === 'SKJEMA_VALIDERT') {
                $location.path('/vedlegg');
            } else {
                $location.path('/soknad');
            }
        }
        $scope.oppsummeringHtml = '';
        $scope.harbekreftet = {value: ''};
        $scope.skalViseFeilmelding = {value: false};

        $scope.soknadId = data.soknad.soknadId;
        $http.get('/sendsoknad/rest/soknad/oppsummering/' + $scope.soknadId).then(function(response) {
            var soknadElement = $(response.data).filter("#soknad");
            soknadElement.find('.logo').remove();
            soknadElement.find('.hode h1').addClass('stor strek-ikon-oppsummering');
            soknadElement.find('hr').remove();
            $scope.oppsummeringHtml = soknadElement.html();
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

                soknadService.send({soknadId: $scope.soknadId},
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