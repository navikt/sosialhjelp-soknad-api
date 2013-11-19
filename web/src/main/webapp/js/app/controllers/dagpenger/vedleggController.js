angular.module('nav.vedlegg.controller', [])
    .controller('VedleggCtrl', ['$scope', 'data', function ($scope, data) {

        $scope.showErrors = false;

        $scope.sidedata = {navn: 'vedlegg'};

        $scope.vedlegg = data.soknadOppsett.vedlegg;
        angular.forEach($scope.vedlegg, function(v){
            v.valg = 'sendinn';
        });
        $scope.soknad = data.soknad;
        $scope.erFaktumLikKriterie = function (vedlegg) {
            if (data.soknad.fakta[vedlegg.faktum.id]) {
                return data.soknad.fakta[vedlegg.faktum.id].value === vedlegg.onValue;
            }
            return false;
        }
        $scope.vedleggBehandlet = function(vedlegg){
            return vedlegg.ikkeSend || vedlegg.sendSenere  || vedlegg.lastetOpp;
        }
    }]);