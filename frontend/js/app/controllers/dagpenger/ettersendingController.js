angular.module('nav.ettersending', [])
.controller('EttersendingCtrl', ['$scope', '$location', 'data', function ($scope, $location, data) {
        $scope.vedleggListe = data.soknad.vedlegg;
    }]);
