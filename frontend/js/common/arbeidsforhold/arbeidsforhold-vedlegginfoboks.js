angular.module('nav.arbeidsforhold.vedlegginfoboks', [])
    .directive('arbeidsforholdVedleggInfoboks', function (cms) {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                vedleggtekster: "=",
                infotekster: "=",
                rotasjonVedlegg: "=?",
                arbeidsforhold: "="
            },
            templateUrl: '../js/common/arbeidsforhold/templates/arbeidsforholdVedleggInfoboksTemplate.html',

            link: {
                pre: function (scope) {
                    scope.alleVedleggtekster = scope.vedleggtekster;
                    if (isNotNullOrUndefined(scope.rotasjonVedlegg)) {
                        var rotasjonTekst = cms.tekster['arbeidsforhold.rotasjonskiftturnus.sporsmaal.alternativ.jarotasjon'];
                        if (scope.arbeidsforhold.properties.rotasjonskiftturnus == rotasjonTekst) {
                            scope.alleVedleggtekster = scope.vedleggtekster.concat("Dagpenger.vedlegg.M6.header");
                        }
                    }
                }
            }
        };
    });