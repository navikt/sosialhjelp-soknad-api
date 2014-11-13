angular.module('nav.arbeidsforhold.permittering.directive',[])
    .directive('permitteringsPeriode', function(Faktum) {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/permitteringsPerioderTemplate.html',
            replace: true,
            scope: true,
            link: {
                pre: function(scope) {
                    scope.leggTilPeriode = function() {
                        var permitteringsperiode = new Faktum({
                            key: 'arbeidsforhold.permitteringsperiode',
                            properties: {}
                        });

                        scope.permitteringsperioder.push(permitteringsperiode);
                        scope.barnefaktum.push(permitteringsperiode);
                    };

                    if(scope.permitteringsperioder.length === 0) {
                        scope.leggTilPeriode();
                    }
                }
            }
        };
    })
    .directive('permitteringsPeriodeOppsummering', function(data) {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/permitteringsPerioderOppsummeringTemplate.html',
            replace: true,
            scope: {
                parentFaktum: '@permitteringsPeriodeOppsummering'
            },
            link: function(scope) {
                scope.permitteringsPerioder = data.finnFakta('arbeidsforhold.permitteringsperiode').filter(function(faktum) {
                    return faktum.parrentFaktum == scope.parentFaktum;
                });
            }
        };
    });