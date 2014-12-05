angular.module('nav.arbeidsforhold.turnus.directive',[])
    .directive('turnusblokkHarRotasjon', function(cms) {
        return {
            replace: false,
            scope: false,
            controller: function () {
                this.harValgtRotasjon = function(arbeidsforhold) {
                    var rotasjonTekst = cms.tekster['arbeidsforhold.rotasjonskiftturnus.sporsmaal.alternativ.jarotasjon'];
                    return arbeidsforhold.properties.rotasjonskiftturnus == rotasjonTekst;
                };
            }
        };
    })
    .directive('turnusblokk', function() {
        return {
            require: 'turnusblokkHarRotasjon',
            templateUrl: '../js/common/arbeidsforhold/templates/undersporsmaal/turnusblokkTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function (scope, element, attrs, turnusblokkHarRotasjon) {
                scope.parentFaktum = scope.arbeidsforhold;
                scope.harValgtRotasjon = turnusblokkHarRotasjon.harValgtRotasjon;
            }
        };
    })
    .directive('turnusblokkOppsummering', function() {
        return {
            require: 'turnusblokkHarRotasjon',

            templateUrl: '../js/common/arbeidsforhold/templates/oppsummeringer/turnusblokkOppsummeringTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function (scope, element, attrs, turnusblokkHarRotasjon) {
                scope.harValgtRotasjon = turnusblokkHarRotasjon.harValgtRotasjon;
            }
        };
    });