angular.module('nav.ettersending.controllers.nyttVedlegg', [])
    .controller('EttersendingNyttVedleggCtrl', function ($scope, data, vedleggService, $location, Faktum, EttersendingVedleggResolver) {

        /*
         * Controlleren må være på formen, og kan derfor ikke legges til i routes. Derfor kan vi ikke injecte
         * resultatet fra vedlegg rett inn i controlleren, og vi må derfor hente det ut sånn her :|
         */
        var vedlegg;
        EttersendingVedleggResolver.then(function(result) {
            vedlegg = result;
        });

        $scope.nyttvedlegg = {};
        $scope.fremdriftsindikator = {
            laster: false
        };

        $scope.lagreVedlegg = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);

            if (form.$valid) {

                new Faktum({
                    key: 'ekstraVedlegg',
                    value: 'true',
                    soknadId: data.soknad.soknadId
                }).$save().then(function (nyttfaktum) {
                        vedleggService.hentAnnetVedlegg({soknadId: data.soknad.soknadId, faktumId: nyttfaktum.faktumId}, function (resultVedlegg) {
                            resultVedlegg.navn = $scope.nyttvedlegg.navn;
                            resultVedlegg.$save();
                            vedlegg.push(resultVedlegg);
                            $location.path('opplasting/' + resultVedlegg.vedleggId);
                        });
                    });
            }
        };
    });