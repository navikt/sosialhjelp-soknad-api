angular.module('nav.fortsettsenere', [])
    .controller('FortsettSenereCtrl', ['$scope', 'data', '$location', 'fortsettSenereService', 'Faktum',
        function ($scope, data, $location, fortsettSenereService, Faktum) {
            var lagretEpost = data.finnFaktum('epost');
            $scope.brukerBehandlingId = data.soknad.brukerBehandlingId;

            if (lagretEpost) {
                $scope.epost = data.finnFaktum('epost');
            } else {
                var personalia = data.finnFaktum('personalia');
                $scope.epost = {
                    key: 'epost',
                    value: undefined
                };
                $scope.epost.value = personalia.properties.epost;
            }

            $scope.forrigeSide = $scope.brukerBehandlingId + '/fortsett';

            $scope.soknadId = data.soknad.soknadId;

            $scope.dittnavUrl = data.config["dittnav.link.url"];

            $scope.fortsettSenere = function (form) {
                $scope.$broadcast('RUN_VALIDATION' + form.$name);

                if (form.$valid) {
                    var behandlingId = getBehandlingIdFromUrl();
                    if ($scope.epost) {
                        $scope.epost = new Faktum($scope.epost);
                        $scope.epost.$save({soknadId: data.soknad.soknadId}).then(function (epostData) {
                            data.leggTilFaktum(epostData);
                            new fortsettSenereService({epost: $scope.epost.value}).$send({behandlingId: behandlingId}).then(function (data) {
                                $location.path($scope.brukerBehandlingId + '/kvittering-fortsettsenere');
                            });
                        });
                    }
                }
            };
        }
    ])
    .controller('FortsettSenereKvitteringCtrl', function ($scope, data) {
        $scope.dittnavUrl = data.config["dittnav.link.url"];
        $scope.epost = data.finnFaktum('epost');
        $scope.forrigeSide = data.soknad.brukerBehandlingId + '/fortett';
    });




