angular.module('nav.arbeidsforhold.directive',[])
    .directive('arbeidsforholdOppsummering', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/oppsummeringer/arbeidsforhold-oppsummering.html',
            replace: true,
            scope: true,
            link: function(scope) {
                scope.templates = {
                    'Sagt opp av arbeidsgiver': { oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/sagt-opp-av-arbeidsgiver-oppsummering.html' },
                    'Permittert': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/permittert-oppsummering.html' },
                    'Kontrakt utgått': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/kontrakt-utgaatt-oppsummering.html'},
                    'Sagt opp selv': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/sagt-opp-selv-oppsummering.html' },
                    'Redusert arbeidstid': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/redusertarbeidstid-oppsummering.html' },
                    'Arbeidsgiver er konkurs': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/konkurs-oppsummering.html'},
                    'Avskjediget': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/avskjediget-oppsummering.html'}
                };
            }
        };
    })
    .directive('arbeidsforholdLeggtilKnapper', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/arbeidsforhold-leggtil-knapper.html',
            replace: true,
            scope: true
        };
    })
    .directive('arbeidsforholdSluttaarsak', function() {
        function getCmsKeyFromSluttaarsak(sluttaarsak) {
            var cmsKey = "arbeidsforhold.sluttaarsak.radio.";
            switch(sluttaarsak) {
                case "Permittert":
                    cmsKey += "permittert";
                    break;
                case "Avskjediget":
                    cmsKey += "avskjediget";
                    break;
                case "Kontrakt utgått":
                    cmsKey += "kontraktutgaatt";
                    break;
                case "Redusert arbeidstid":
                    cmsKey += "redusertArbeidstid";
                    break;
                case "Arbeidsgiver er konkurs":
                    cmsKey += "arbeidsgiverErKonkurs";
                    break;
                case "Sagt opp av arbeidsgiver":
                    cmsKey += "sagtOppAvArbeidsgiver";
                    break;
                case "Sagt opp selv":
                    cmsKey += "sagtOppSelv";
                    break;
                default:
                    cmsKey += "default";
                    break;
            }
            return cmsKey;
        }

        return {
            template: '<p data-ng-bind-html="cmsnokkel | cmstekst"></p>',
            replace: true,
            scope: {
                sluttaarsak: '='
            },
            link: function(scope) {
                scope.cmsnokkel = getCmsKeyFromSluttaarsak(scope.sluttaarsak);
            }
        };
    });