//TODO: Hele modulen burde vel fjernes? Flytte det som skal beholdes til egne moduler
angular.module('app.brukerdata', ['app.services'])
    // TODO: Denne skal vel bort?
    .controller('SoknadDataCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.soknadData = data.soknad;
    }])
    // TODO: Flytte til egen modul?
    .controller('AvbrytCtrl', function ($scope, $routeParams, $location, soknadService, data) {
        var soknadId = data.soknad.soknadId;
        $scope.krevBekreftelse = {value: ''};

        $scope.fremdriftsindikator = {
            laster: false
        }

        soknadService.get({soknadId: soknadId}).$promise.then(function (result) {
            var fakta = $.map(result.fakta, function (element) {
                return element.type;
            });
            $scope.krevBekreftelse.value = $.inArray("BRUKERREGISTRERT", fakta) > 0;

            if (!$scope.krevBekreftelse.value) {
                $scope.submitForm();
            }
        })

        $scope.submitForm = function () {
            var start = $.now();
            $scope.fremdriftsindikator.laster = true;
            soknadService.remove({soknadId: soknadId},
                function () { // Success
                    var delay = 1500 - ($.now() - start);
                    setTimeout(function () {
                        $scope.$apply(function () {
                            $location.path('/slettet');
                        });
                    }, delay);
                },
                function () { // Error
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };
    })
    // TODO: Denne skal vel bort?
    .directive('modFaktum', function () {
        return function ($scope, element, attrs) {
            var eventType;
            switch (element.attr('type')) {
                case "radio":
                case "checkbox":
                    eventType = "change";
                    break;
                default:
                    eventType = "blur";
            }

            element.bind(eventType, function () {
                var verdi = element.val().toString();
                if (element.attr('type') === "checkbox") {
                    verdi = element.is(':checked').toString();
                }

                if ($scope.faktum) {
                    $scope.faktum.$save();
                } else {
                    $scope.$apply(function () {
                        $scope.$emit("OPPDATER_OG_LAGRE", {key: element.attr('name'), value: verdi});
                    });
                }

            });
        };
    })
    // TODO: Denne skal vel bort?
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
