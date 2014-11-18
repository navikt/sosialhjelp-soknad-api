angular.module('nav.oppsummering', [])
    .controller('OppsummeringCtrl', ['$scope', 'data', '$location', 'soknadService', '$http', function ($scope, data, $location, soknadService, $http) {
        $scope.oppsummeringHtml = '';
        $scope.harbekreftet = {value: ''};
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.soknadId = data.soknad.soknadId;
        $http.get('/sendsoknad/rest/soknad/oppsummering/' + $scope.soknadId).then(function(response) {
            var soknadElement = angular.element(response.data.replace(/http:\/\/[^\/]*/g, '')).filter("#soknad");
            soknadElement.find('.logo').remove();
            soknadElement.find('.hode h1').addClass('stor strek-ikon-oppsummering');
            soknadElement.find('hr').remove();
            $scope.oppsummeringHtml = soknadElement.html();
        });

        $scope.sendSoknad = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;

            if ($scope.harbekreftet.value) {
                $scope.fremdriftsindikator.laster = true;

                soknadService.send({soknadId: $scope.soknadId},
                    //Success
                    function () {
                        $location.path('bekreftelse/' + data.soknad.brukerBehandlingId);
                    },
                    //Error
                    function () {
                        $scope.fremdriftsindikator.laster = false;
                        $location.path('feilside');
                    }
                );
            } else {
                $scope.$broadcast(eventString);
            }
        };
    }])
    .filter('formatterFnr', [function () {
        return function (fnr) {
            return fnr.substring(0, 6) + " " + fnr.substring(6, fnr.length);
        };
    }]);