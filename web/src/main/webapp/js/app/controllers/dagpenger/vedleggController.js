angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', 'Faktum', function ($scope, $routeParams, vedleggService, Faktum) {
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
            return forventning.faktum.properties['vedlegg_' + forventning.gosysId] == status;
        };
        $scope.slettVedlegg = function (forventning) {
            if($scope.erEkstraVedlegg(forventning)){
                $scope.slettAnnetVedlegg(forventning);
            }
            forventning.$slettVedlegg().then(function () {
                forventning.faktum.properties['vedlegg_' + forventning.gosysId] = 'VedleggKreves';
                forventning.vedlegg = null;
            });
        };
        $scope.key = function (forventning) {
            return 'vedlegg_' + forventning.gosysId;
        };
        $scope.endreInnsendingsvalg = function (forventning, valg) {
            if (valg != undefined) {
                forventning.faktum.properties['vedlegg_' + forventning.gosysId] = valg;
            }
            new Faktum(forventning.faktum).$save();
        };
        $scope.erEkstraVedlegg = function(forventning){
            return forventning.gosysId == 'L6';
        }
        $scope.slettAnnetVedlegg = function(forventning){
            var index = $scope.forventninger.indexOf(forventning);
            Faktum.delete({soknadId: data.soknad.soknadId, faktumId: forventning.faktum.faktumId});
            $scope.forventninger.splice(index, 1);
        }
        $scope.nyttAnnetVedlegg = function () {
            new Faktum({
                key: 'ekstraVedlegg',
                value: 'true',
                soknadId: data.soknad.soknadId
            }).$save().then(function (nyttfaktum) {
                    VedleggForventning.query({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function(forventninger){
                        $scope.forventninger.push.apply($scope.forventninger, forventninger);
                    });
                });
        };
    }])
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        }
    }]);