angular.module('nav.arbeidsforhold.permittering.directive',[])
    .directive('permitteringsPeriodeInput', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/permitteringsPeriodeInputTemplate.html',
            replace: true,
            scope: {
                permitteringsperiode: '='
            },
            link: function(scope) {
                scope.feilmelding = {
                    required:'arbeidsforhold.sluttaarsak.permittert.permitteringsgrad.feilmelding',
                    pattern: 'regex.tall',
                    max: 'arbeidsforhold.sluttaarsak.permittert.permitteringsgrad.maxverdi.feilmelding',
                    min:'arbeidsforhold.sluttaarsak.permittert.permitteringsgrad.minverdi.feilmelding'
                }
            }
        };
    })
    .directive('permitteringsPeriodeInfo', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/permitteringsPeriodeInfoTemplate.html',
            replace: true,
            scope: {
                permitteringsperiode: '=',
                endrePermitteringsperiode: '&',
                slettPermitteringsperiode: '&'
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