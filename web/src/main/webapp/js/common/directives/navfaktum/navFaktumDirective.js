angular.module('nav.navfaktum', [])
    .directive('navFaktum', [function () {
        return {
            replace: false,
            scope: true,
            controller: ['$scope', '$attrs', '$filter', 'data', 'Faktum', function ($scope, $attrs, $filter, data, Faktum) {
                var props = $scope.$eval($attrs.navProperty);
                $scope.ikkeAutoLagre = $attrs.ikkeAutoLagre;
                var satt = false;

                if ($scope[$attrs.navFaktum]) {
                    $scope.faktum = $scope[$attrs.navFaktum];
                    satt = true;
                } else {
                    data.fakta.forEach(function (faktum) {
                        if (faktum.key === $attrs.navFaktum) {
                            $scope.faktum = faktum;
                            satt = true;
                        }
                    });
                }

                if (!satt) {
                    $scope.faktum = new Faktum({
                            key: $attrs.navFaktum,
                            soknadId: data.soknad.soknadId,
                            properties: {}
                        }
                    );
                    data.fakta.push($scope.faktum);
                }
                $scope.parentFaktum = $scope.faktum;

                if (props) {
                    $scope.navproperties = {}
                    props.forEach(function (prop) {
                        var val = $scope.faktum.properties[prop];
                        if (val && val.match(/\d\d\d\d\.\d\d\.\d\d/)) {
                            val = new Date(val);
                        }
                        $scope.navproperties[prop] = val;
                    });
                }

                $scope.lagreFaktum = function () {
                    if (props) {
                        props.forEach(function (prop) {
                            var value = $scope.navproperties[prop];
                            if (value != undefined) {
                                if (angular.isDate(value)) {
                                    value = $filter('date')(value, 'yyyy.MM.dd')
                                } else {
                                    value = value.toString();
                                }
                            }
                            $scope.parentFaktum.properties[prop] = value;
                        })
                    }
                    if(!$scope.ikkeAutoLagre) {
                        $scope.parentFaktum.$save();
                    }
                }
                this.lagreFaktum = $scope.lagreFaktum;
            }
            ]
        }
    }])
;