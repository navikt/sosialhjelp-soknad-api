angular.module('nav.ettersending.controllers.opplasting', [])
    .controller('EttersendingOpplastingCtrl', function ($scope, $routeParams, vedleggService, vedlegg) {
        $scope.vedleggListe = vedlegg;
        $scope.vedleggListe.forEach(function (v) {
            if (v.vedleggId == $routeParams.vedleggId) {
                $scope.vedlegg = new vedleggService(v);
            }
        });

        $scope.ettersend = {
            valgt: $scope.vedlegg.innsendingsvalg === 'SendesSenere'
        };

        $scope.settTilEttersendes = function () {
            if ($scope.ettersend.valgt) {
                $scope.vedlegg.innsendingsvalg = 'SendesSenere';
            } else {
                $scope.vedlegg.innsendingsvalg = $scope.vedlegg.opprinneligInnsendingsvalg;
            }

            $scope.vedlegg.$save();
        };

        $scope.kanEndreInnsendingsvalg = function () {
            return $scope.vedlegg.opprinneligInnsendingsvalg === 'SendesIkke';
        };
    });