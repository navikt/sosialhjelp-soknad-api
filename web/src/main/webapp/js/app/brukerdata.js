angular.module('app.brukerdata', ['app.services'])
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
    })

    .controller('AvbrytCtrl', function ($scope, $routeParams, $location, soknadService) {
        $scope.fremdriftsindikator = {
            laster: false
        }
        $scope.data = {}
        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            var fakta = $.map(result.fakta, function (element) {
                return element.type;
            });
            $scope.data.krevBekreftelse = $.inArray("BRUKERREGISTRERT", fakta) > 0;

            if (!$scope.data.krevBekreftelse) {
                $scope.submitForm();
            }
        })

        $scope.submitForm = function () {
            var start = $.now();
            $scope.fremdriftsindikator.laster = true;
            soknadService.remove({param: $routeParams.soknadId},
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
    })

    .filter('midlertidigAdresseType', function () {
        return function (input, scope) {
            var tekst;
            switch (input) {
                case "MIDLERTIDIG_POSTADRESSE_NORGE":

                    tekst = "tekster.personalia_midlertidig_adresse_norge";
                    break;
                case "MIDLERTIDIG_POSTADRESSE_UTLAND":
                    tekst = scope.tekster.personalia_midlertidig_adresse_utland;
                    break;
                default :
                    //TODO: fix
                    tekst = "Du har ikke midlertidig adresse i norge eller utlandet";
            }
            return tekst;
        }
    });
