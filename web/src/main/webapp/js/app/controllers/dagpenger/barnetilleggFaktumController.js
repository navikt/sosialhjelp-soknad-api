angular.module('nav.barnetilleggfaktum', ['app.services'])
    .controller('BarnetilleggFaktumCtrl', ['$scope', 'Faktum', function ($scope, Faktum) {
        var barnetilleggsData = {
            key: 'barnetillegg',
            value: false,
            parrentFaktum: $scope.b.faktumId
        };

        var ikkebarneinntekt = {
            key: 'ikkebarneinntekt',
            value: false,
            parrentFaktum: $scope.b.faktumId
        };

        var barneinntekttall = {
            key: 'barneinntekttall',
            value: undefined,
            parrentFaktum: $scope.b.faktumId
        };

        if ($scope.soknadData.fakta.barnetillegg) {
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function (value) {
                if (value.parrentFaktum == $scope.b.faktumId) {
                    barnetilleggsData = value;
                }
            });
        }
        if ($scope.soknadData.fakta.ikkebarneinntekt) {
            angular.forEach($scope.soknadData.fakta.ikkebarneinntekt.valuelist, function (value) {
                if (value.parrentFaktum == $scope.b.faktumId) {
                    ikkebarneinntekt = value;
                }
            });
        }
        if ($scope.soknadData.fakta.barneinntekttall) {
            angular.forEach($scope.soknadData.fakta.barneinntekttall.valuelist, function (value) {
                if (value.parrentFaktum == $scope.b.faktumId) {
                    barneinntekttall = value;
                }
            });
        }

        $scope.barnetillegg = new Faktum(barnetilleggsData);
        $scope.$watch('barnetillegg.value', function (newValue, oldValue, scope) {
            if (newValue != undefined && newValue !== oldValue) {
                scope.barnetillegg.$save({soknadId: scope.soknadData.soknadId}).then(function (data) {
                    scope.barnetillegg = data;
                });
            }
        });

        $scope.ikkebarneinntekt = new Faktum(ikkebarneinntekt);
        $scope.$watch('ikkebarneinntekt.value', function (newValue, oldValue, scope) {
            if (newValue != undefined && newValue !== oldValue) {
                scope.ikkebarneinntekt.$save({soknadId: scope.soknadData.soknadId}).then(function (data) {
                    scope.ikkebarneinntekt = data;
                });
            }
        });

        $scope.barneinntekttall = new Faktum(barneinntekttall);
        $scope.$watch('barneinntekttall.value', function (newValue, oldValue, scope) {
            if (newValue != undefined && newValue !== oldValue) {
                scope.barneinntekttall.$save({soknadId: scope.soknadData.soknadId}).then(function (data) {
                    scope.barneinntekttall = data;
                });
            }
        });

        $scope.barnetilleggErRegistrert = function () {
            return $scope.barnetillegg.value == 'true';
        }

        $scope.barnetilleggIkkeRegistrert = function () {
            return !$scope.barnetilleggErRegistrert();
        }


        $scope.barnetHarInntekt = function () {
            if ($scope.ikkebarneinntekt == undefined) {
                return false;
            }
            return sjekkOmGittEgenskapTilObjektErFalse($scope.ikkebarneinntekt);
        }

        $scope.barnetHarIkkeInntekt = function () {
            return !$scope.barnetHarInntekt();
        }

        $scope.slettBarnetillegg = function (faktumId, index, $event) {
            $event.preventDefault();
            var barnetilleggsData;
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function (value) {
                if (value.parrentFaktum == faktumId) {
                    barnetilleggsData = value;
                }
            });

            $scope.barnetilleggSomSkalSlettes = new Faktum(barnetilleggsData);

            $scope.barnetilleggSomSkalSlettes.$delete({soknadId: $scope.soknadData.soknadId}).then(function () {
                $scope.barnetillegg.value = "false";
                $scope.soknadData.fakta.barnetillegg.valuelist.splice(index, 1);
            });
        }

    }]);