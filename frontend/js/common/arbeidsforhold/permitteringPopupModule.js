angular.module('nav.arbeidsforhold.permitteringpopup', [])
    .factory('permitteringPopup', ['$modal', function ($modal, $scope) {
        return {
            'openPopup': function (permitteringer, permittering) {
                $modal.open({
                    templateUrl: '../js/common/arbeidsforhold/templates/permitteringPopupTemplate.html',
                    controller: 'PermitteringPopupCtrl',
                    scope: $scope,
                    windowClass: 'smalt',
                    resolve: {
                        permitteringer: function () {
                            return permitteringer;
                        },
                        permittering: function () {
                            return permittering;
                        }
                    }
                });
            }
        };
    }])
    .controller('PermitteringPopupCtrl', function ($scope, $modalInstance, permitteringer, permittering, Faktum) {
        $scope.lukk = function () {
            var fokusElement = angular.element(".knapp-leggtil-liten");
            scrollToElement(fokusElement, 200);
            fokusElement.focus();
            $modalInstance.close();

        };
        $scope.originalPermitteringsperiode = permittering;
        $scope.endreModus = permittering ? true : false;
        $scope.permitteringsperiode = $scope.endreModus ? angular.copy(permittering) : new Faktum({key: 'arbeidsforhold.permitteringsperiode', properties: {}});

        $scope.alleAndrePermitteringsperioder = permitteringer.filter(function (periode) {
            return periode !== permittering;
        });
        $scope.cmsPostFix = $scope.endreModus ? ".endre" : "";

        $scope.lagreNyPermitteringsPeriode = function (nypermittering) {
            permitteringer.push(nypermittering);
        };
    })
    .controller('permitteringsperiodeNyttCtrl', function ($scope) {
        $scope.lagrePermitteringsperiode = function (form) {
            var eventString = 'RUN_VALIDATION' + form.$name;
            $scope.$broadcast(eventString);

            $scope.runValidation(true);

            if (form.$valid) {
                if (!$scope.endreModus) {
                    $scope.lagreNyPermitteringsPeriode($scope.permitteringsperiode);
                } else {
                    oppdaterEksisterendePermitteringsPeriode($scope.originalPermitteringsperiode, $scope.permitteringsperiode);
                }
                $scope.lukk();
            }
        };

        $scope.avbryt = function () {
            $scope.lukk();
        };

        initialiserValideringAvIntervall();

        function initialiserValideringAvIntervall() {
            $scope.datoIntervallErValidert = {value: "true"};
            $scope.$watchCollection('[permitteringsperiode.properties.permiteringsperiodedatofra, permitteringsperiode.properties.permiteringsperiodedatotil]',
                function () {
                    $scope.datoIntervallErValidert.value = validerAtPeriodeIkkeOverlapperAndrePerioder() ? "true" : "";
                });
        }

        function oppdaterEksisterendePermitteringsPeriode(original, nyData) {
            angular.forEach(original, function (value, key) {
                original[key] = nyData[key];
            });
        }

        function validerAtPeriodeIkkeOverlapperAndrePerioder() {
            var permittering = $scope.permitteringsperiode.properties;
            var fraDato = new Date(permittering.permiteringsperiodedatofra);
            var sluttDato = permittering.permiteringsperiodedatotil ? new Date(permittering.permiteringsperiodedatotil) : new Date();

            for (var i = 0; i < $scope.alleAndrePermitteringsperioder.length; i++) {
                var periode = $scope.alleAndrePermitteringsperioder[i];
                var periodeFra = new Date(periode.properties.permiteringsperiodedatofra);
                var periodeTil = periode.properties.permiteringsperiodedatotil ? new Date(periode.properties.permiteringsperiodedatotil) : new Date();

                if (erDatoMellomIntervall(fraDato, periodeFra, periodeTil) ||
                    erDatoMellomIntervall(sluttDato, periodeFra, periodeTil) ||
                    erDatoMellomIntervall(periodeFra, fraDato, sluttDato)) {
                    return false;
                }
            }
            return true;
        }

        function erDatoMellomIntervall(dato, startDato, sluttDato) {
            return dato >= startDato && dato <= sluttDato;
        }
    });