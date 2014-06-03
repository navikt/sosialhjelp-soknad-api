angular.module('nav.ettersending.controllers.avbryt', [])
    .controller('EttersendingAvbrytCtrl', function ($scope, data, ettersendingService, $timeout, vedlegg) {
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.slettEttersending = function() {
            $scope.fremdriftsindikator.laster = true;
            var start = $.now();

            ettersendingService.delete({soknadId: data.soknad.soknadId},
                {},
                function() {
                    var delay = 1500 - ($.now() - start);
                    $timeout(function() {
                        if ($scope.harLastetOppNoenDokumenter()) {
                            redirectTilSide('/sendsoknad/ettersending/avbrutt');
                        } else {
                            redirectTilMineHenvendelser();
                        }
                    }, delay);
                },
                function() {
                    $scope.fremdriftsindikator.laster = false;
                }
            );
        };


        $scope.harLastetOppNoenDokumenter = function() {
            return vedlegg.filter(function (v) {
                return v.storrelse > 0;
            }).length > 0;
        };

        if (!$scope.harLastetOppNoenDokumenter()) {
            $scope.slettEttersending();
        }

        function redirectTilMineHenvendelser() {
            redirectTilUrl(data.config['saksoversikt.link.url']);
        }
    });
