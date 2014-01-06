angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', function ($scope, $routeParams, vedleggService) {
        $scope.vedlegg = vedleggService.get({
            soknadId: $routeParams.soknadId,
            faktumId: $routeParams.faktumId,
            vedleggId: $routeParams.vedleggId
        });
    }])
    .controller('VedleggCtrl', ['$scope', '$location', '$routeParams', '$anchorScroll', 'data', 'VedleggForventning', function ($scope, $location, $routeParams, $anchorScroll, data, VedleggForventning) {

        $scope.forventninger = VedleggForventning.query({soknadId: data.soknad.soknadId});
        $scope.sidedata = {navn: 'vedlegg'};
        $scope.lastetOpp = function (forventning) {
            return forventning.faktum.vedleggId;
        }
        $scope.slettVedlegg = function (forventning) {
            forventning.$slettVedlegg().then(function () {
                forventning.faktum.innsendingsvalg = 'VedleggKreves';
                forventning.faktum.vedleggId = null;
                forventning.vedlegg = null;
            });
        }
        $scope.endreInnsendingsvalg = function (forventning) {
            forventning.$endreValg().then(function (data) {

            });
        }
    }])
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        }
    }]);