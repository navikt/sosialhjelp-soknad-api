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
                    $location.path('reell-arbeidssoker/' + result.id);
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
        $scope.data = {};

        tpsService.get({soknadId: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.personalia = result;

              //TODO: For adresse-testing
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":"POSTBOKS","postboksNummer":"1234","adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_NORGE","gatenavn":"Kirkeveien","husnummer":"55","husbokstav":"D","postnummer":"7000","poststed":"Trondheim","land":null,"gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":null});
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"MIDLERTIDIG_POSTADRESSE_UTLAND","gatenavn":null,"husnummer":null,"husbokstav":null,"postnummer":null,"poststed":null,"land":"SVERIGE","gyldigTil":1412373600000,"gyldigFra":1380895717011,"postboksNavn":null,"postboksNummer":null,"adresseEier":"Per P. Nilsen","utenlandsAdresse":["Öppnedvägen 22","1234, Udevalla"]});
//            Mangler eksempel på mildertidig omrodeadresse
//            $scope.personalia.fakta.adresser.push({"soknadId":1,"type":"BOSTEDSADRESSE","gatenavn":"Blåsbortveien","husnummer":"24","husbokstav":"","postnummer":"0368","poststed":"Malmö","land":"SVERIGE","gyldigFra":null,"gyldigTil":null,"utenlandsAdresse":null,"adresseEier":null,"postboksNummer":null,"postboksNavn":null});

            $scope.personalia.fakta.adresser.forEach(function (data, index) {
                if (data.type === "BOSTEDSADRESSE") {
                    $scope.data.bostedsAdresse = index;
                } else if (data.type === "POSTADRESSE") {
                    $scope.data.postAdresse = index;
                } else {
                    $scope.data.midlertidigAdresse = index;
                }
            });

            // Trenger kanskje ikkje != undefined
            $scope.harBostedsAdresse = function () {
                return $scope.data.bostedsAdresse != undefined;
            }

            $scope.harMidlertidigAdresse = function () {
                return $scope.data.midlertidigAdresse != undefined;
            }

            $scope.harBostedsadresseOgIngenMidlertidigAdresse = function() {
                return !$scope.harMidlertidigAdresse() && $scope.harBostedsAdresse();        
            }

            $scope.harPostboksAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].postboksNavn != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].postboksNummer != undefined;
            }
            $scope.harGateAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].gatenavn != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].husnummer != undefined
            }

            $scope.harMidlertidigUtenlandskAdresse = function () {
                return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].utenlandsAdresse.length > 0
            }

            $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse = function() {
                return $scope.harMidlertidigAdresse() &&  $scope.harBostedsAdresse() && $scope.personalia.fakta.adresser[$scope.data.bostedsAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.data.bostedsAdresse].land != "";   
            }

            $scope.harUtenlandskAdresse = function() {
                return $scope.harMidlertidigUtenlandskAdresse() || $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse();
            }
            $scope.hentMidlertidigAdresseTittel = function() {
                if (!$scope.harMidlertidigAdresse()) {
                    return;
                }

                var tekst;
                var type = $scope.personalia.fakta.adresser[$scope.data.midlertidigAdresse].type;
                switch (type) {
                    case "MIDLERTIDIG_POSTADRESSE_NORGE":
                        tekst = data.tekster["personalia.midlertidig_adresse_norge"];
                        break;
                    case "MIDLERTIDIG_POSTADRESSE_UTLAND":
                        tekst = data.tekster["personalia.midlertidig_adresse_utland"];
                        break;
                    default :
                        //TODO: fix
                        tekst = "Du har ikke midlertidig adresse i norge eller utlandet";
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
    }])

    .controller('SoknadDataCtrl', function ($scope, $routeParams, $location, $timeout, soknadService) {
        $scope.soknadData = soknadService.get({param: $routeParams.soknadId});

        $scope.$on("OPPDATER_OG_LAGRE", function (e, data) {
            $scope.soknadData.fakta[data.key] = {"soknadId": $scope.soknadData.soknadId, "key": data.key, "value": data.value};
            $scope.$apply();
            var soknadData = $scope.soknadData;
            soknadData.$save({param: soknadData.soknadId, action: 'lagre'});
            console.log("lagre: " + soknadData);
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

    .controller('UtdanningCtrl', function ($scope) {
        $scope.hvisIkkeUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'ikkeUtdanning';
            }
            return false;
        }

        $scope.hvisAvsluttetUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'avsluttetUtdanning';
            }
            return false;
        }

        $scope.hvisUnderUtdanning = function () {
            if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.utdanning != undefined) {
                return $scope.soknadData.fakta.utdanning.value == 'underUtdanning';
            }
            return false;
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

    .controller('ArbeidsforholdCtrl', function ($scope) {
        $scope.arbeidsforhold = {};
        $scope.nyttArbeidsforhold = function ($event) {
            var key = 'arbeidsforhold' + Object.keys($scope.arbeidsforhold).length;
            $scope.arbeidsforhold[key] = {};
        }

        // Lagre på ferdig-knappen per arbeidsforhold
        $scope.$on("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD", function (e) {
            $scope.soknadData.fakta.arbeidsforhold = {"soknadId": $scope.soknadData.soknadId, "key": "arbeidsforhold",
                "value": JSON.stringify($scope.arbeidsforhold)};
            var soknadData = $scope.soknadData;
            soknadData.$save({param: soknadData.soknadId, action: 'lagre'});
            $scope.$apply();
            console.log("lagre: " + soknadData.soknadId);
        });
    })

    .directive('lagreArbeidsforhold', function () {
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
                $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD");
            });
        };
    })

    .directive('legg-til-arbeidsforhold', function () {
        return function ($scope, element, attrs) {
            element.click(function () {
                
            })
        }

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
                $scope.$emit("OPPDATER_OG_LAGRE", {key: attrs.name, value: verdi});
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

