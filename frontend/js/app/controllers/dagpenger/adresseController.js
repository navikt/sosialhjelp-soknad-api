angular.module('nav.adresse', [])
    .controller('AdresseCtrl', ['$scope', 'cms', 'data', function($scope, cms, data) {
        $scope.personalia = data.finnFaktum('personalia').properties;

        $scope.harGjeldendeAdresse = function() {
            return $scope.personalia.gjeldendeAdresse != null;
        };

        $scope.formattertGjeldendeAdresse = '';
        $scope.gjeldendeAdresseTypeLabel = '';

        $scope.hentFormattertAdresse = function(adresse) {
            var formattertAdresse = '';

            var adresseLinjer = adresse.split(',');

            adresseLinjer.forEach(function(adresseLinje) {
                formattertAdresse += '<p>' + adresseLinje.trim() + '</p>';
            });
            return formattertAdresse;
        };

        $scope.hentAdresseTypeNokkel = function(adresseType) {
            if (adresseType === 'UTENLANDSK_ADRESSE' || adresseType === 'BOSTEDSADRESSE' || adresseType === 'POSTADRESSE') {
                return 'personalia.folkeregistrertadresse';
            } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_NORGE') {
                return 'MIDLERTIDIG_POSTADRESSE_NORGE';
            } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_UTLAND') {
                return 'personalia.midlertidigAdresseUtland';
            } else {
                return '';
            }
        };

        if ($scope.harGjeldendeAdresse()) {
            $scope.formattertGjeldendeAdresse = $scope.hentFormattertAdresse($scope.personalia.gjeldendeAdresse);
            $scope.gjeldendeAdresseTypeLabel = $scope.hentAdresseTypeNokkel($scope.personalia.gjeldendeAdresseType);
        }

        $scope.harSekundarAdresse = function() {
            return $scope.personalia.sekundarAdresse != null;
        };

        $scope.formattertSekundarAdresse = '';
        $scope.sekundarAdresseTypeLabel = '';
        if ($scope.harSekundarAdresse()) {
            $scope.formattertSekundarAdresse = $scope.hentFormattertAdresse($scope.personalia.sekundarAdresse);
            $scope.sekundarAdresseTypeLabel = $scope.hentAdresseTypeNokkel($scope.personalia.sekundarAdresseType);
        }

       


        
    }]);