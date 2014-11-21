angular.module('nav.arbeidsforhold.turnus.directive',[])
    .directive('turnusblokk', function(cms) {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/undersporsmaal/turnusblokkTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function (scope) {
                scope.parentFaktum = scope.arbeidsforhold;
                scope.harValgtRotasjon = function() {
                    var rotasjonTekst = cms.tekster['arbeidsforhold.rotasjonskiftturnus.sporsmaal.alternativ.jarotasjon'];
                    return scope.arbeidsforhold.properties.rotasjonskiftturnus == rotasjonTekst;
                };
            }
        };
    })
    .directive('turnusblokkOppsummering', function(cms) {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/oppsummeringer/turnusblokkOppsummeringTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function(scope) {
                scope.harValgtRotasjon = function() {
                    var rotasjonTekst = cms.tekster['arbeidsforhold.rotasjonskiftturnus.sporsmaal.alternativ.jarotasjon'];
                    return scope.arbeidsforhold.properties.rotasjonskiftturnus == rotasjonTekst;
                };
            }
        };
    });