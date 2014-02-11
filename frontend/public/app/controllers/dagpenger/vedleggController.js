angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', function ($scope, $routeParams, vedleggService) {
        $scope.vedlegg = vedleggService.get({
            soknadId: $routeParams.soknadId,
            faktumId: $routeParams.faktumId,
            vedleggId: $routeParams.vedleggId
        });
    }])
    .controller('VedleggCtrl', ['$scope', '$location', '$routeParams', '$anchorScroll', 'data', 'VedleggForventning', 'Faktum', function ($scope, $location, $routeParams, $anchorScroll, data, VedleggForventning, Faktum) {

        $scope.forventninger = VedleggForventning.query({soknadId: data.soknad.soknadId});
        $scope.sidedata = {navn: 'vedlegg'};
        $scope.vedleggEr = function (forventning, status) {
            return forventning.faktum.properties['vedlegg_' + forventning.skjemaNummer] === status;
        };
        $scope.slettVedlegg = function (forventning) {
            forventning.$slettVedlegg().then(function () {
                forventning.faktum.properties['vedlegg_' + forventning.skjemaNummer] = 'VedleggKreves';
                forventning.vedlegg = null;
            });
        };
        $scope.key = function (forventning) {
            return 'vedlegg_' + forventning.skjemaNummer;
        };
        $scope.endreInnsendingsvalg = function (forventning, valg) {
            if (valg !== undefined) {
                forventning.faktum.properties['vedlegg_' + forventning.skjemaNummer] = valg;
            }
            new Faktum(forventning.faktum).$save();
        }
    }])
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        }
    }]);
