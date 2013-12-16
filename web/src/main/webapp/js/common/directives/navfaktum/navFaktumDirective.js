angular.module('nav.navfaktum', [])
    .directive('navFaktum', [function () {
        return {
            replace: false,
            controller: ['$scope', '$attrs', 'data', 'Faktum', function ($scope, $attrs, data, Faktum) {
                var satt = false;
                data.fakta.forEach(function (faktum) {
                    if (faktum.key === $attrs.navFaktum) {
                        $scope.faktum = faktum;
                        satt = true;
                    }
                });
                if (!satt) {
                    $scope.faktum = new Faktum({
                            key: $attrs.navFaktum,
                            soknadId: data.soknad.soknadId
                        }
                    );
                    data.fakta.push($scope.faktum);
                }
                $scope.parentFaktum = $scope.faktum;
                if ($attrs.navProperty) {
                    $scope.faktum = {key: $attrs.navProperty, value: $scope.parentFaktum.properties[$attrs.navProperty]};
                }

                $scope.lagreFaktum = function () {
                    if (!$scope.faktum.soknadId) {
                        //$scope.parentFaktum.properties[$scope.faktum.key] = $scope.faktum.value;
                    }
                    $scope.parentFaktum.$save();
                }
                this.lagreFaktum = $scope.lagreFaktum;
            }
            ]
        }
    }])
;