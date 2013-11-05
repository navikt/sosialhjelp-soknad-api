angular.module('nav.vedlegg.controller', [])
    .controller('VedleggCtrl', ['$scope', 'data', function ($scope, data) {

        $scope.showErrors = false;

        $scope.sidedata = {navn: 'vedlegg'};

        $scope.vedlegg = data.soknadOppsett.vedlegg;
        $scope.soknad = data.soknad;
        $scope.erFaktumLikKriterie = function (vedlegg) {
            return data.soknad.fakta[vedlegg.faktum.id].value === vedlegg.onValue;
        }
        $scope.vedleggBehandlet = function(vedlegg){
            return vedlegg.ikkeSend || vedlegg.sendSenere  || vedlegg.lastetOpp;
        }
    }]);