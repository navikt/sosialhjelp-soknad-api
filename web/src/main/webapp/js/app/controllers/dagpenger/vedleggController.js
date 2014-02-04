angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', 'Faktum', 'data', function ($scope, $routeParams, vedleggService, Faktum, data) {
        $scope.vedlegg = vedleggService.get({
            soknadId: data.soknad.soknadId,
            vedleggId: $routeParams.vedleggId
        });
    }])

    .controller('VedleggCtrl', ['$scope', '$location', '$routeParams', '$anchorScroll', 'data', 'vedleggService', 'Faktum', 'VedleggForventning', function ($scope, $location, $routeParams, $anchorScroll, data, vedleggService, Faktum, VedleggForventning) {
        $scope.data = {soknadId: data.soknad.soknadId};
        $scope.forventninger = vedleggService.query({soknadId: data.soknad.soknadId});
        $scope.sidedata = {navn: 'vedlegg'};
        $scope.validert = {value: ''};

        $scope.validerVedlegg = function (form) {
            if (form.$valid) {
                $location.path('/oppsummering');
            } else {
                $scope.validert.value = true;
            }
            $scope.runValidation(true);
        }

        $scope.vedleggEr = function (vedlegg, status) {
            return vedlegg.innsendingsvalg === status;
        };

        $scope.nyttAnnetVedlegg = function () {
            new Faktum({
                key: 'ekstraVedlegg',
                value: 'true',
                soknadId: data.soknad.soknadId
            }).$save().then(function (nyttfaktum) {
                    VedleggForventning.query({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (forventninger) {
                        $scope.forventninger.push.apply($scope.forventninger, forventninger);
                    });
                });
        };
    }])

    .controller('validervedleggCtrl', ['$scope', 'Faktum', function ($scope, Faktum) {
        if ($scope.forventning.innsendingsvalg === "VedleggKreves") {
            $scope.hiddenFelt = {value: '' };
            $scope.skalViseFeil = { value: true };
        } else {
            $scope.hiddenFelt = {value: 'true' };
            $scope.skalViseFeil = { value: false };
        }


        $scope.slettVedlegg = function (forventning) {
            if ($scope.erEkstraVedlegg(forventning)) {
                $scope.slettAnnetVedlegg(forventning);
            }
            forventning.$remove().then(function () {
                forventning.innsendingsvalg = 'VedleggKreves';
            });

            $scope.skalViseFeil = { value: true };
            $scope.validert.value = false;
        };

        $scope.lagreVedlegg = function (forventning) {
            forventning.$save();
        };

        $scope.key = function (forventning) {
            return 'vedlegg_' + forventning.skjemaNummer;
        };

        $scope.endreInnsendingsvalg = function (forventning, valg) {
            if (valg !== 'SendesSenere' && valg !== 'SendesIkke') {
                forventning.innsendingsvalg = valg;
            }
            if (!$scope.hiddenFelt) {
                $scope.hiddenFelt = { value: "" };
                $scope.skalViseFeil = { value: "" };
            }

            if (forventning.innsendingsvalg === valg) {
                forventning.innsendingsvalg = "VedleggKreves";
                $scope.hiddenFelt.value = "";
                $scope.skalViseFeil.value = true;
                forventning.$save();
            } else {
                forventning.innsendingsvalg = valg;
                forventning.$save();

                $scope.hiddenFelt.value = true;
                $scope.skalViseFeil.value = false;
            }
        };

        $scope.erEkstraVedlegg = function (forventning) {
            return forventning.skjemaNummer === 'N6';
        };

        $scope.slettAnnetVedlegg = function (forventning) {
            var index = $scope.forventninger.indexOf(forventning);
            Faktum.delete({soknadId: forventning.soknadId, faktumId: forventning.faktumId});
            $scope.forventninger.splice(index, 1);
            $scope.skalViseFeil = { value: true };
            $scope.validert.value = false;
        };
    }])

    .filter('nospace', function () {
        return function (value) {
            return (!value) ? '' : value.replace(/ /g, '');
        };
    })
    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        }
    }]);
