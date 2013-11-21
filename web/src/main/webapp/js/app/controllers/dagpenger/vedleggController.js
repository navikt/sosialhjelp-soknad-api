angular.module('nav.vedlegg.controller', [])
    .controller('VedleggCtrl', ['$scope', 'data', function ($scope, data) {

        function cloneObject(object) {
            return $.extend({}, object);
        }

        function opprettVedleggMedFaktum(key) {
            var vedlegg = cloneObject(vedleggMap[key]);
            vedlegg.data = data.soknad.fakta[key];
            vedlegg.valg = 'sendinn';
            vedlegg.lastetOpp = function(){
                return vedlegg.data.vedleggId;
            }
            return vedlegg;
        }

        function faktumMedVedlegg(key) {
            return vedleggMap[key] != undefined;
        }
        function indekserVedlegg(vedlegg){
            var vedleggMap = {};
            vedlegg.forEach(function (vedlegg) {
                var id = vedlegg.faktum.id;
                vedleggMap[id] = vedlegg;
            });
            return vedleggMap;
        }


        var vedleggMap = indekserVedlegg(data.soknadOppsett.vedlegg);
        $scope.sidedata = {navn: 'vedlegg'};
        $scope.vedlegg = Object.keys(data.soknad.fakta).filter(faktumMedVedlegg).map(opprettVedleggMedFaktum);
        $scope.soknad = data.soknad;
        $scope.erFaktumLikKriterie = function (vedlegg) {
            if (data.soknad.fakta[vedlegg.faktum.id]) {
                return data.soknad.fakta[vedlegg.faktum.id].value === vedlegg.onValue;
            }
            return false;
        }
        $scope.vedleggBehandlet = function (vedlegg) {
            return vedlegg.valg === 'ikkeSend' || vedlegg.valg == 'sendSenere' || vedlegg.lastetOpp();
        }
    }]);