angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', function ($scope, $routeParams, vedleggService) {
        $scope.vedlegg = vedleggService.get({
            soknadId: $routeParams.soknadId,
            faktumId: $routeParams.faktumId,
            vedleggId: $routeParams.vedleggId
        });
    }])
    .controller('VedleggCtrl', ['$scope', '$location', '$routeParams', '$anchorScroll', 'data', 'vedleggService', function ($scope, $location, $routeParams, $anchorScroll, data, vedleggService) {

        function cloneObject(object) {
            return $.extend({}, object);
        }

        function opprettVedleggMedFaktum(key) {
            var vedlegg = cloneObject(vedleggMap[key]);
            vedlegg.data = data.soknad.fakta[key];
            vedlegg.valg = 'sendinn';
            vedlegg.side = 0;
            vedlegg.lastetOpp = function () {
                return vedlegg.data.vedleggId;
            }
            vedlegg.$naviger = function (antall) {
                vedlegg.side = vedlegg.side + antall;
                console.log("naviger " + vedlegg.side)
            }
            if (vedlegg.lastetOpp()) {
                console.log("lastet opp: " + vedlegg.data.id);
                vedlegg.vedlegg = vedleggService.get({
                    soknadId: vedlegg.data.soknadId,
                    faktumId: vedlegg.data.id,
                    vedleggId: vedlegg.data.vedleggId
                })
            }

            return vedlegg;
        }

        function faktumMedVedlegg(key) {
            return vedleggMap[key] != undefined;
        }

        function indekserVedlegg(vedlegg) {
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
        $scope.slettVedlegg = function (vedlegg) {

            vedleggService.remove({
                soknadId: data.soknad.soknadId,
                faktumId: vedlegg.data.id,
                vedleggId: vedlegg.data.vedleggId}, function () {
                vedlegg.data.vedleggId = null;
            });

        }
        $scope.vedleggBehandlet = function (vedlegg) {
            return vedlegg.valg === 'ikkeSend' || vedlegg.valg == 'sendSenere' || vedlegg.lastetOpp();
        }
    }])
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        }

    }]);