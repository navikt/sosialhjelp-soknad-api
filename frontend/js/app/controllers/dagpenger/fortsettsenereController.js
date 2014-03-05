angular.module('nav.fortsettsenere', ['nav.cmstekster'])
    .controller('FortsettSenereCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', 'fortsettSenereService', 'Faktum',
        function ($scope, data, $routeParams, $http, $location, fortsettSenereService, Faktum) {
            var lagretEpost = data.finnFaktum('epost');

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

            if (!$scope.forrigeSide) {
                $scope.forrigeSide = '/soknad';
            }

            $scope.soknadId = data.soknad.soknadId;

            $scope.dittNavUrl = data.config["soknad.dittnav.link.url"];

            $scope.fortsettSenere = function (form) {
                $scope.$broadcast('RUN_VALIDATION' + form.$name);

                if (form.$valid) {
                    var behandlingId = getBehandlingIdFromUrl();
                    if ($scope.epost) {
                        $scope.epost = new Faktum($scope.epost);
                        $scope.epost.$save({soknadId: data.soknad.soknadId}).then(function (epostData) {
                            data.leggTilFaktum(epostData);
                            new fortsettSenereService({epost: $scope.epost.value}).$send({behandlingId: behandlingId}).then(function (data) {
                                $location.path('kvittering-fortsettsenere');
                            });
                        });
                    }
                }
            };
        }
    ])
    .controller('FortsettSenereKvitteringCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
        $scope.epost = data.finnFaktum('epost');

        if (!$scope.forrigeSide) {
            $scope.forrigeSide = '/soknad';
        }
    }
    ]);




