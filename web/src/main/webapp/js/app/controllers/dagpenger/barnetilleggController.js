angular.module('nav.barnetillegg', [])
    .controller('BarnetilleggCtrl', ['$scope', '$cookieStore', '$location', '$timeout', 'Faktum', function ($scope, $cookieStore, $location, $timeout, Faktum) {
        $scope.erBrukerregistrert = function (barn) {
            return barn.type == 'BRUKERREGISTRERT';
        }

        $scope.erSystemRegistrert = function (barn) {
            return  barn.type == 'SYSTEMREGISTRERT';
        }

        $scope.ingenLandRegistrert = function (barn) {
            return !barn.properties.land
        }

        $scope.leggTilBarn = function ($event) {
            $event.preventDefault();
            settBarnCookie();
            $location.path('nyttbarn/' + $scope.soknadData.soknadId);
        }

        $scope.endreBarn = function (faktumId, $event) {
            $event.preventDefault();
            settBarnCookie(faktumId);
            $location.path('endrebarn/' + $scope.soknadData.soknadId + "/" + faktumId);
        }

        $scope.sokbarnetillegg = function (faktumId, $event) {
            $event.preventDefault();
            settBarnCookie(faktumId);
            $location.path('sokbarnetillegg/' + $scope.soknadData.soknadId + "/" + faktumId);
        }

        $scope.slettBarn = function (b, index, $event) {
            $event.preventDefault();
            $scope.barnSomSkalSlettes = new Faktum(b);

            $scope.barnSomSkalSlettes.$delete({soknadId: $scope.soknadData.soknadId}).then(function () {
                $scope.soknadData.fakta.barn.valuelist.splice(index, 1);
            });
        }


        $scope.erGutt = function (barn) {
            return barn.properties.kjonn == "gutt";
        }

        $scope.erJente = function (barn) {
            return barn.properties.kjonn == "jente";
        }

        $scope.validerBarnetillegg = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        function settBarnCookie(faktumId) {
            var aapneTabIds = [];
            angular.forEach($scope.grupper, function (gruppe) {
                if (gruppe.apen) {
                    aapneTabIds.push(gruppe.id);
                }
            });

            $cookieStore.put('barn', {
                aapneTabs: aapneTabIds,
                gjeldendeTab: "#barnetillegg",
                faktumId: faktumId
            })
        }

    }]);