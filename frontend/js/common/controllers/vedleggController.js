angular.module('nav.vedlegg.controller', [])
    .controller('VisVedleggCtrl', ['$scope', '$routeParams', 'vedleggService', 'data', function ($scope, $routeParams, vedleggService, data) {
        $scope.vedlegg = vedleggService.get({
            soknadId: data.soknad.soknadId,
            vedleggId: $routeParams.vedleggId
        });
    }])
    .controller('VedleggCtrl', function ($scope, $location, data, vedleggService, Faktum, soknadService, $timeout) {
        $scope.data = {soknadId: data.soknad.soknadId};
        $scope.forventninger = vedleggService.query({soknadId: data.soknad.soknadId});
        $scope.brukerBehandlingId = data.soknad.brukerBehandlingId;
        $scope.sidedata = {navn: 'vedlegg'};
        $scope.soknadOppsett = data.soknadOppsett;

        $scope.validert = {value: ''};
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.validerVedlegg = function (form) {
            $scope.fremdriftsindikator.laster = true;
            if (form.$valid) {
                soknadService.delsteg({soknadId: data.soknad.soknadId, delsteg: 'oppsummering'},
                    function() {
                        $location.path(data.soknad.brukerBehandlingId + '/oppsummering');
                    },
                    function() {
                        $scope.fremdriftsindikator.laster = false;
                    }
                );
            } else {
                $scope.fremdriftsindikator.laster = false;
                $scope.validert.value = true;
                $timeout(function () {
                    $scope.leggTilStickyFeilmelding();
                }, 50);

            }
            $scope.runValidation(true);
        };

        $scope.vedleggEr = function (vedlegg, status) {
            return vedlegg.innsendingsvalg === status;
        };

        $scope.vedleggFerdigBehandlet = function(forventning) {
            return $scope.ekstraVedleggFerdig(forventning) && !$scope.vedleggEr(forventning, 'VedleggKreves');
        };

        $scope.ekstraVedleggFerdig = function (forventning) {
            if(forventning.skjemaNummer === 'N6') {
                return forventning.navn !== null && forventning.navn !== undefined;
            }
            return true;
        };

        $scope.nyttAnnetVedlegg = function () {
            new Faktum({
                key: 'ekstraVedlegg',
                value: 'true',
                soknadId: data.soknad.soknadId
            }).$save().then(function (nyttfaktum) {
                    vedleggService.hentAnnetVedlegg({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (forventninger) {
                        $scope.forventninger.push(forventninger);
                    });
                });
        };
    })

    .controller('validervedleggCtrl', function ($scope, Faktum) {
        if ($scope.forventning.innsendingsvalg === "VedleggKreves") {
            $scope.hiddenFelt = {value: '' };
            $scope.skalViseFeil = { value: true };
        } else {
            $scope.hiddenFelt = {value: 'true' };
            $scope.skalViseFeil = { value: false };
        }

        $scope.filVedlagt = $scope.forventning.storrelse === 0 ? "" : "true";


        $scope.slettVedlegg = function (forventning) {
            if ($scope.erEkstraVedlegg(forventning)) {
                $scope.slettAnnetVedlegg(forventning);
            }
            forventning.$remove().then(function () {
                forventning.innsendingsvalg = 'VedleggKreves';

            });

            $scope.hiddenFelt = {value: '' };
            $scope.skalViseFeil = { value: true };
            $scope.validert.value = false;
        };

        $scope.lagreVedlegg = function (forventning) {
            forventning.$save();
        };

        $scope.key = function (forventning) {
            return 'vedlegg_' + forventning.skjemaNummer;
        };

        $scope.fjernInnsendingsvalg = function(forventning) {
            forventning.innsendingsvalg = 'VedleggKreves';
        };

        $scope.endreInnsendingsvalg = function (hiddenValue, skalViseFeil) {
            $scope.hiddenFelt.value = hiddenValue;
            $scope.skalViseFeil.value = skalViseFeil;
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

        $scope.skalViseNesteKnapp = function(forventning, erSiste) {
            return !(($scope.erEkstraVedlegg(forventning) && forventning.innsendingsvalg !== 'LastetOpp') || erSiste);
        };

        $scope.skalViseAlleredeSendtAlternativ = function(forventing) {
            var vedlegg = $scope.finnVedleggMedSkjemanummer(forventing.skjemaNummer);
            return (vedlegg && vedlegg.ekstraValg && vedlegg.ekstraValg.indexOf("AlleredeSendt") > -1);
        };

        $scope.finnVedleggMedSkjemanummer = function(skjemanummer) {
            for(var i=0; i<$scope.soknadOppsett.vedlegg.length; i++) {
                if($scope.soknadOppsett.vedlegg[i].skjemaNummer == skjemanummer) {
                    return $scope.soknadOppsett.vedlegg[i];
                }
            }
        };
    })

    .directive('bildeNavigering', [function () {
        return {
            restrict: 'a',
            replace: 'true',
            templateUrl: '../../'
        };
    }])
    .directive('fjernRadioValg', function() {
        return {
            scope:  {
                forventning: '=fjernRadioValg'
            },
            link: function(scope, element, attrs) {
                var forrigeValg = scope.forventning.innsendingsvalg;
                var valg = attrs.value;
                element.bind('click', function() {
                    if (scope.forventning.innsendingsvalg === forrigeValg) {
                        scope.forventning.innsendingsvalg = 'VedleggKreves';
                        scope.$parent.endreInnsendingsvalg('', true);
                        scope.forventning.$save();
                    } else {
                        scope.forventning.innsendingsvalg = valg;
                        scope.forventning.$save();
                        scope.$parent.endreInnsendingsvalg(true, false);
                    }
                    forrigeValg = scope.forventning.innsendingsvalg;
                    scope.$apply();
                });
            }
        };
    });
