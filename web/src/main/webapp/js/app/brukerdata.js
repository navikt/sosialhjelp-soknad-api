angular.module('app.brukerdata', ['app.services'])

    .controller('StartSoknadCtrl', function ($scope, $location, soknadService) {
        $scope.data = {
            laster: false
        };
        $scope.startSoknad = function () {
            var soknadType = window.location.pathname.split("/")[3];
            $scope.data.laster = true;
            $scope.soknad = soknadService.create({param: soknadType},
                function (result) {
                    $location.path('dagpenger/' + result.id);
                    $scope.data.laster = false;
                }, function () {
                    $scope.data.laster = false;
                });
        }
    })

    .controller('SendSoknadCtrl', function ($scope, $location, $routeParams, soknadService) {
        $scope.sendSoknad = function () {
            soknadService.send({param: $routeParams.soknadId, action: 'send'});
            $location.path('kvittering');
        }
    })


    .controller('PersonaliaCtrl', ["$scope", "$routeParams", "tpsService", "data", function ($scope, $routeParams, tpsService, data) {
        $scope.personaliaData = {};

        tpsService.get({soknadId: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.personalia = result;
              //TODO: For adresse-testing
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":"POSTBOKS","postboksNummer":"1234","adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//            Mangler eksempel på mildertidig omrodeadresse
             
//             $scope.personalia.fakta.adresser = [];
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//             $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"UTENLANDSK_ADRESSE","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"BOSTEDSADRESSE","gatenavn":"Blåsbortveien","husnummer":"24","husbokstav":"","postnummer":"0368","poststed":"Malmö","land":"SVERIGE","gyldigFra":null,"gyldigTil":null,"utenlandsAdresse":null,"adresseEier":null,"postboksNummer":null,"postboksNavn":null});

            $scope.personalia.fakta.adresser.forEach(function (data, index) {
                if (data.type === "BOSTEDSADRESSE") {
                    $scope.personaliaData.bostedsAdresse = index;
                } else if (data.type === "POSTADRESSE") {
                    $scope.personaliaData.postAdresse = index;
                } else if (data.type === "UTENLANDSK_ADRESSE") {
                    $scope.personaliaData.utenlandskAdresse = index;
                } else {
                    $scope.personaliaData.midlertidigAdresse = index;
                }
            });

            $scope.harBostedsAdresse = function () {
                return $scope.personaliaData.bostedsAdresse != undefined;
            }

            $scope.c = function () {
                return $scope.personaliaData.utenlandskAdresse != undefined;
            }

            $scope.harMidlertidigAdresse = function () {
                return $scope.personaliaData.midlertidigAdresse != undefined;
            }

            $scope.harMidlertidigAdresseEier = function () {
                return $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].adresseEier != undefined;
            }

            $scope.harBostedsadresseOgIngenMidlertidigAdresse = function() {
                return !$scope.harMidlertidigAdresse() && $scope.harBostedsAdresse();        
            }

            $scope.harPostboksAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].postboksNavn != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].postboksNummer != undefined;
            }
            $scope.harGateAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].gatenavn != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].husnummer != undefined
            }

            $scope.harMidlertidigUtenlandskAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].utenlandsAdresse.length > 0
            }

            $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse = function() {
                return $scope.harMidlertidigAdresse() &&  $scope.harUtenlandskPostAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.utenlandskAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.utenlandskAdresse].land != "";
            }

            $scope.harUtenlandskAdresse = function() {
                return $scope.harMidlertidigUtenlandskAdresse() || $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse();
            }
            $scope.hentMidlertidigAdresseTittel = function() {
                if (!$scope.harMidlertidigAdresse()) {
                    return;
                }

                var tekst;
                var type = $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].type;
                switch (type) {
                    case "MIDLERTIDIG_POSTADRESSE_NORGE":
                        tekst = data.tekster["personalia.midlertidig_adresse_norge"];
                        break;
                    case "MIDLERTIDIG_POSTADRESSE_UTLAND":
                        tekst = data.tekster["personalia.midlertidig_adresse_utland"];
                        break;
                    default :
                        tekst = data.tekster["personalia.ingenadresse"];
                }
                return tekst;
            }
        });

        $scope.harHentetPersonalia = function() {
            return $scope.personalia != undefined;
        }

        $scope.harIkkeHentetPersonalia = function() {
            return !$scope.harHentetPersonalia();
        }

        $scope.validerPersonalia = function(form) {
            // Har ikke form her ennå
            $scope.validateForm(false);
        }
    }])

    .controller('SoknadDataCtrl', function ($scope, $routeParams, $location, $timeout, soknadService) {
        $scope.soknadData = soknadService.get({param: $routeParams.soknadId});

        $scope.$on("OPPDATER_OG_LAGRE", function (e, data) {
            $scope.soknadData.fakta[data.key] = {"soknadId": $scope.soknadData.soknadId, "key": data.key, "value": data.value};
            var soknadData = $scope.soknadData;
            soknadData.$save({param: soknadData.soknadId, action: 'lagre'});
        });
    })

    .controller('TekstCtrl', function ($scope, tekstService) {
        $scope.tekster = tekstService.get({side: 'Dagpenger'});
    })

    .controller('ModusCtrl', function ($scope) {
        $scope.data = {
            showErrorMessage: false,
            redigeringsModus: true
        };
        $scope.showErrors = false;

        $scope.validateForm = function (invalid) {
            $scope.showErrors = invalid;
            $scope.data.showErrorMessage = invalid;
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

        $scope.visFeilmeldinger = function () {
            $scope.data.showErrorMessage = true;
            $scope.showErrors = true;
        }

        $scope.hvisIkkeFormValiderer = function () {
            return $scope.data.showErrorMessage;
        }
    })

    .controller('AvbrytCtrl', function ($scope, $routeParams, $location, soknadService) {
        $scope.data = {
            laster: false
        };
        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            var fakta = $.map(result.fakta, function (element) {
                return element.type;
            });
            $scope.data.krevBekreftelse = $.inArray("BRUKERREGISTRERT", fakta) >= 0;

            if (!$scope.data.krevBekreftelse) {
                $scope.submitForm();
            }
        })

        $scope.submitForm = function () {
            var start = $.now();
            $scope.data.laster = true;
            soknadService.delete({param: $routeParams.soknadId},
                function () { // Success
                    var delay = 1500 - ($.now() - start);
                    setTimeout(function () {
                        $scope.$apply(function () {
                            $scope.data.laster = false;
                            $location.path('slettet');
                        });
                    }, delay);
                },
                function () { // Error
                    $scope.data.laster = false;
                }
            );
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
                var verdi = element.val().toString();
                if (element.attr('type') === "checkbox") {
                    verdi = element.is(':checked').toString();
                }

                $scope.$apply(function() {
                    $scope.$emit("OPPDATER_OG_LAGRE", {key: element.attr('name'), value: verdi});
                });
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
    });

