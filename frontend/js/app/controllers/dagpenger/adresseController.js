angular.module('nav.adresse', [])
    .controller('AdresseCtrl', ['$scope', 'cms', 'data', function($scope, cms, data) {
        $scope.personalia = data.finnFaktum('personalia').properties;

        $scope.harGjeldendeAdresse = function() {
            return $scope.personalia.gjeldendeAdresse != null;
        };

        $scope.formattertGjeldendeAdresse = '';
        $scope.gjeldendeAdresseTypeLabel = '';
        if ($scope.harGjeldendeAdresse()) {
            $scope.formattertGjeldendeAdresse = hentFormattertAdresse($scope.personalia.gjeldendeAdresse);
            $scope.gjeldendeAdresseTypeLabel = hentAdresseTypeNokkel($scope.personalia.gjeldendeAdresseType);
        }

        $scope.harSekundarAdresse = function() {
            return $scope.personalia.sekundarAdresse != null;
        };

        $scope.formattertSekundarAdresse = '';
        $scope.sekundarAdresseTypeLabel = '';
        if ($scope.harSekundarAdresse()) {
            $scope.formattertSekundarAdresse = hentFormattertAdresse($scope.personalia.sekundarAdresse);
            $scope.sekundarAdresseTypeLabel = hentAdresseTypeNokkel($scope.personalia.sekundarAdresseType);
        }

        function hentFormattertAdresse(adresse) {
            var formattertAdresse = '';

            var adresseLinjer = adresse.split(',');

            adresseLinjer.forEach(function(adresseLinje) {
                formattertAdresse += '<p>' + adresseLinje.trim() + '</p>';
            });
            return formattertAdresse;
        };

        function hentAdresseTypeNokkel(adresseType) {
            if (adresseType === 'UTENLANDSK_ADRESSE' || 'BOSTEDSADRESSE' || 'POSTADRESSE') {
                return 'personalia.folkeregistrertadresse';
            } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_NORGE') {
                return 'personalia.midlertidigAdresseNorge';
            } else if (adresseType === 'MIDLERTIDIG_POSTADRESSE_UTLAND') {
                return 'personalia.midlertidigAdresseUtland';
            } else {
                return '';
            }
        };
    }]);