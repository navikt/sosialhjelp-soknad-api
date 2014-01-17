angular.module('nav.adresse', [])
    .controller('AdresseCtrl', ['$scope', 'personalia', 'cms', function($scope, personalia, cms) {
        $scope.personaliaData = {};
        $scope.personalia = personalia;

        if ($scope.personalia.fakta.adresser != undefined) {
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
        } else {
            $scope.personalia.fakta.adresser = [];
        }

        $scope.harBostedsAdresse = function () {
            return $scope.personaliaData.bostedsAdresse != undefined;
        }

        $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse = function () {
            return $scope.harMidlertidigAdresse() && $scope.harUtenlandskPostAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.utenlandskAdresse].land != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.utenlandskAdresse].land != "";
        }

        $scope.harUtenlandskAdresse = function () {
            return $scope.harUtenlandskFolkeregistrertAdresseOgMidlertidigNorskAdresse();
        }

        $scope.harUtenlandskPostAdresse = function () {
            return $scope.personaliaData.utenlandskAdresse != undefined;
        }

        $scope.harMidlertidigAdresse = function () {
            return $scope.personaliaData.midlertidigAdresse != undefined;
        }

        $scope.harNorskMidlertidigAdresse = function () {
            return $scope.harPostboksAdresse() || $scope.harGateAdresse() || $scope.harOmrodeAdresse();
        }

        $scope.harMidlertidigAdresseEier = function () {

            return $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].adresseEier != undefined;
        }

        $scope.harBostedsadresseOgIngenMidlertidigAdresse = function () {
            return !$scope.harMidlertidigAdresse() && $scope.harBostedsAdresse();
        }

        $scope.harPostboksAdresse = function () {
            return $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].postboksNavn != undefined || $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].postboksNummer != undefined;
        }
        $scope.harGateAdresse = function () {
            return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].gatenavn != undefined && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].husnummer != undefined
        }

        $scope.harOmrodeAdresse = function () {
            return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].eiendomsnavn != undefined;
        }

        $scope.hentMidlertidigAdresseTittel = function () {
            if (!$scope.harMidlertidigAdresse()) {
                return;
            }

            var tekst;
            var type = $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].type;
            switch (type) {
                case "MIDLERTIDIG_POSTADRESSE_NORGE":
                    tekst = cms.tekster["personalia.midlertidig_adresse_norge"];
                    break;
                case "MIDLERTIDIG_POSTADRESSE_UTLAND":
                    tekst = cms.tekster["personalia.midlertidig_adresse_utland"];
                    break;
                default :
                    tekst = cms.tekster["personalia.ingenadresse"];
            }
            return tekst;
        }

        $scope.harMidlertidigUtenlandskAdresse = function () {
            return $scope.harMidlertidigAdresse() && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].land != undefined
                && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].utenlandsAdresse != undefined
                && $scope.personalia.fakta.adresser[$scope.personaliaData.midlertidigAdresse].utenlandsAdresse.length > 0;
        }
    }]);