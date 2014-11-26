angular.module('nav.gjenopptak.informasjonsside', [])
    .controller('InformasjonsSideCtrl', function ($scope, data, $location, soknadService) {
        $scope.utslagskriterier = {};
        $scope.utslagskriterier.harlestbrosjyre = false;
        $scope.cmsprefix = "gjenopptak";
        $scope.dittnavUrl = data.config["dittnav.link.url"];
        $scope.tilbakeUrl = '../utslagskriterier/dagpenger';

        $scope.oppsummering = false;

        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.tpsSvarer = function () {
            return !$scope.tpsSvarerIkke();
        };

        $scope.tpsSvarerIkke = function () {
            if ($scope.utslagskriterier.error !== undefined) {
                return true;
            }
            return false;
        };

        $scope.soknadErIkkeStartet = function () {
            return !$scope.soknadErStartet();
        };

        $scope.soknadErStartet = function () {
            return !(data.soknad === undefined || data.soknad.brukerBehandlingId === undefined);
        };

        $scope.soknadErIkkeFerdigstilt = function () {
            return !$scope.soknadErFerdigstilt();
        };

        $scope.soknadErFerdigstilt = function () {
            return data && data.soknad && data.soknad.status === "FERDIG";
        };

        $scope.startSoknad = function () {
            var soknadType = getSoknadstypeFromUrl();

            $scope.fremdriftsindikator.laster = true;
            $scope.soknad = soknadService.create({soknadType: soknadType},
                function (result) {
                    $location.path(result.brukerbehandlingId + "/soknad/");
                }, function () {
                    $scope.fremdriftsindikator.laster = false;
                });
        };

        $scope.harLestBrosjyre = function () {
            return $scope.utslagskriterier.harlestbrosjyre;
        };

        $scope.startSoknadDersomBrosjyreLest = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;

            if (form.$valid) {
                $scope.startSoknad();
            } else {
                $scope.$broadcast(eventString);
            }
        };

        $scope.forsettSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $location.path(data.soknad.brukerBehandlingId + "/fortsett");
            }
        };

        if ($scope.soknadErStartet()) {
            $scope.utslagskriterier.harlestbrosjyre = true;
            $scope.oppsummering = true;
        }

    });
