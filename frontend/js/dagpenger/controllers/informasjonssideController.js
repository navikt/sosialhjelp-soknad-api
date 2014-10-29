angular.module('nav.informasjonsside', ['nav.cmstekster'])
    .controller('InformasjonsSideCtrl', ['$scope', 'data', '$location', 'soknadService', function ($scope, data, $location, soknadService) {
        $scope.utslagskriterier = {};
        $scope.utslagskriterier.harlestbrosjyre = false;
        $scope.cmsprefix = "dagpenger";

        $scope.oppsummering = false;
        if (erSoknadStartet()) {
            $scope.utslagskriterier.harlestbrosjyre = true;
            $scope.oppsummering = true;
        }

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
            if (erSoknadStartet()) {
                return true;
            }
            return false;
        };

        $scope.soknadErIkkeFerdigstilt = function () {
            return !$scope.soknadErFerdigstilt();
        };

        $scope.soknadErFerdigstilt = function () {
            return data && data.soknad && data.soknad.status === "FERDIG";
        };

        $scope.startSoknad = function () {
            var soknadType = decodeURI(window.location.pathname).split("/")[3];
            $scope.fremdriftsindikator.laster = true;
            $scope.soknad = soknadService.create({soknadType: soknadType},
                function (result) {
                    $location.path("/soknad/" + result.brukerbehandlingId);
                }, function () {
                    $scope.fremdriftsindikator.laster = false;
                });
        };

        $scope.harLestBrosjyre = function () {
            return $scope.utslagskriterier.harlestbrosjyre;
        };

        $scope.startSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $scope.startSoknad();
            }
        };

        $scope.forsettSoknadDersomBrosjyreLest = function () {
            if ($scope.harLestBrosjyre()) {
                $location.path("/soknad");
            }
        };

    }])
    .directive('validerInformasjonsside', [function () {
        return {
            link: function (scope, element) {
                var formElement = element.closest('form').find('.form-linje');
                var input = formElement.find('input');
                element.bind('click', function () {
                    if (!input.is(':checked')) {
                        formElement.addClass('feilstyling');
                    }
                });
            }
        };
    }]);
