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

            $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];

            $scope.forsettSenere = function (form) {
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
    ])

    .directive('navGjenoppta', ['$compile', 'data', function ($compile, data) {

        var getForDelsteg = function (delstegstatus) {
            var templateUrl = '';
            switch (delstegstatus) {
                case 'UTFYLLING':
                    templateUrl = '../views/templates/gjenoppta/skjema-under-arbeid.html';
                    break;
                case 'VEDLEGG_VALIDERT':
                    templateUrl = '../views/templates/gjenoppta/skjema-ferdig.html';
                    break;
                case 'SKJEMA_VALIDERT':
                    templateUrl = '../views/templates/gjenoppta/skjema-validert.html';
                    break;
                default:
                    templateUrl = '../views/templates/gjenoppta/skjema-under-arbeid.html';

            }
            return templateUrl;
        };

        var getTemplateUrl = function (status, delstegstatus) {
            var templateUrl = '';
            switch (status) {
                case 'UNDER_ARBEID':
                    templateUrl = getForDelsteg(delstegstatus);
                    break;
                case 'FERDIG':
                    templateUrl = '../views/templates/gjenoppta/skjema-sendt.html';
                    break;
                case 'AVBRUTT':
                    break;
            }
            return templateUrl;
        };


        var linker = function (scope, element, attrs) {
            return getTemplateUrl(data.soknad.status, data.soknad.delstegStatus);
        };

        return{
            restrict: 'A',
            replace: true,
            templateUrl: linker
        };
    }]);


