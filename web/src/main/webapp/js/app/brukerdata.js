angular.module('app.brukerdata', ['app.services'])

    .controller('StartSoknadCtrl', function ($scope, $location, soknadService, enonicService) {
        $scope.startSoknad = function () {
            var soknadType = window.location.pathname.split("/")[3];

            $scope.soknad = soknadService.create({param: soknadType}).$promise.then(function (result) {
                $location.path('reell-arbeidssoker/' + result.id);
            }).finally(function() {
                $('#start').show();
                $('#start').siblings('img').hide();
            })

            // Pre-fetche alle tekster så det blir cachet i angular-land
            $scope.tekster = enonicService.get({side: 'Dagpenger'});
        }
    })

    .controller('SendSoknadCtrl', function ($scope, $location, $routeParams, soknadService) {
        $scope.sendSoknad = function () {
            soknadService.send({param: $routeParams.soknadId, action: 'send'});
            $location.path('kvittering');
        }
    })


    .controller('PersonaliaCtrl', function ($scope, $routeParams, tpsService) {
        $scope.data = {};

        tpsService.get({soknadId: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.personalia = result;

            //TODO: For testing
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":"POSTBOKS","postboksNummer":"1234","adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});

            $scope.personalia.fakta.adresser.forEach(function (data, i) {
                if (data.type === "BOSTEDSADRESSE") {
                    $scope.data.bostedsAdresse = i;
                } else if (data.type === "POSTADRESSE") {
                    $scope.data.postAdresse = i;
                } else {
                    $scope.data.midlertidigAdresse = i;
                }
            });

            $scope.harBostedsAdresse = function () {
                return $scope.data.bostedsAdresse != undefined;
            }

            $scope.harMidlertidigAdresse = function () {
                return $scope.data.midlertidigAdresse != undefined;
            }
            $scope.harPostboksAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].postboksNavn != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].postboksNummer != undefined;
            }
            $scope.harGateAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].gatenavn != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].husnummer != undefined
            }

            $scope.harUtenlandskAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].utenlandsAdresse.length > 0
            }

            $scope.hentMidlertidigAdresseTittel = function() {
                if (!$scope.harMidlertidigAdresse()) {
                    return;
                }

                var tekst;
                var type = $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].type;
                switch (type) {
                    case "MIDLERTIDIG_POSTADRESSE_NORGE":
                        tekst = $scope.tekster.personalia_midlertidig_adresse_norge;
                        break;
                    case "MIDLERTIDIG_POSTADRESSE_UTLAND":
                        tekst = $scope.tekster.personalia_midlertidig_adresse_utland;
                        break;
                    default :
                        //TODO: fix
                        tekst = "Du har ikke midlertidig adresse i norge eller utlandet";
                }
                return tekst;
            }
        });

        $scope.harHenterPersonalia = function() {
            return $scope.personalia != undefined;
        }

        $scope.harIkkeHenterPersonalia = function() {
            return !$scope.harHenterPersonalia();
        }
    })

    .controller('SoknadDataCtrl', function ($scope, $routeParams, $location, $timeout, soknadService) {
        $scope.soknadData = soknadService.get({param: $routeParams.soknadId});

        $scope.lagre = function () {
            var soknadData = $scope.soknadData;
            console.log("lagre: " + soknadData);
            soknadData.$save({param: soknadData.soknadId, action: 'lagre'});
        };

        $scope.avbryt = function () {
            $location.path('avbryt/' + $routeParams.soknadId);
        }
    })

    .controller('TekstCtrl', function ($scope, enonicService) {
        $scope.tekster = enonicService.get({side: 'Dagpenger'});
    })

    .controller('ModusCtrl', function ($scope) {
        $scope.data = {
            showErrorMessage: false,
            redigeringsModus: true
        };

        $scope.validateForm = function (invalid) {
            $scope.data.showErrorMessage = invalid;
            $scope.data.redigeringsModus = invalid;
        }

        $scope.gaTilRedigeringsmodus = function () {
            $scope.data.redigeringsModus = true;
        }

        $scope.hvisIRedigeringsmodus = function () {
            return $scope.data.redigeringsModus;
        }

        $scope.hvisIOppsummeringsmodus = function () {
            return !$scope.hvisIRedigeringsmodus();
        }

        $scope.hvisIkkeFormValiderer = function () {
            return $scope.data.showErrorMessage;
        }
    })

    .controller('AvbrytCtrl', function ($scope, $routeParams, $location, soknadService) {
        $scope.data = {};
        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            var fakta = $.map(result.fakta, function (element) {
                return element.type;
            });
            $scope.data.krevBekreftelse = $.inArray("BRUKERREGISTRERT", fakta) >= 0;

            if (!$scope.data.krevBekreftelse) {
                $scope.submitForm();
            }
        });



        $scope.submitForm = function () {
            var start = $.now();
            soknadService.delete({param: $routeParams.soknadId}).$promise.then(function () {

                // For å forhindre at lasteindikatoren forsvinner med en gang
                var delay = 1500 - ($.now() - start);
                setTimeout(function () {
                    $scope.$apply(function () {
                        $location.path('slettet');
                    });
                }, delay);
            }).finally(function(){
                $('.knapp-advarsel-liten').show();
                $('.knapp-advarsel-liten').siblings("img").hide();
            });;
        };
    })


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
                var verdi = element.val();
                if (element.attr('type') === "checkbox") {
                    verdi = element.is(':checked');
                }

                $scope.soknadData.fakta[attrs.name] = {"soknadId": $scope.soknadData.soknadId, "key": attrs.name, "value": verdi};
                $scope.$apply();
                $scope.lagre();
            });
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
    })


    .factory('time', function ($timeout) {
        var time = {};

        (function tick() {
            time.now = new Date().toString();
            $timeout(tick, 1000);
        })();
        return time;
    })

    .controller('SistLagretCtrl', function ($scope, time) {
        $scope.time = time;
    });

