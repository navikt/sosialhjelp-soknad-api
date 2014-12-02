angular.module('nav.tidligerearbeidsforhold.directive', [])
    .directive('navTidligereArbeidsforhold', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsforholdTemplate.html'
        };
    })
    .directive('permittertSporsmal', function (data) {
        return {
            replace: true,
            scope: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/varpermittertsist.html',
            link: function(scope) {
                scope.varPermittertFaktum = data.finnFaktum('tidligerearbeidsforhold.permittert');
                scope.hvisVarPermittert = function() {
                    return scope.varPermittertFaktum.value == "permittert";
                };
                scope.hvisVarPermittertFiske = function() {
                    return scope.varPermittertFaktum.value == "permittertFiske";
                };
            }
        };
    })
    .directive('tidligereArbeidsgiver', function(data, cms) {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsgiver.html',
            link: function(scope) {
                scope.cmsArgumenter = {
                    modelFaktum: data.finnFaktum('tidligerearbeidsforhold.tidligerearbeidsgiver'),
                    default: cms.tekster["tidligerearbeidsforhold.tidligerearbeidsgiver.defaultnavn"]
                };
                scope.jobbetSidenSistFaktum = data.finnFaktum('tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsidensist');
                scope.hvisHarArbeidet = function() {
                    return scope.jobbetSidenSistFaktum.value == "false";
                };

                scope.jobbetSammenhengendeFaktum = data.finnFaktum('tidligerearbeidsforhold.tidligerearbeidsgiver.jobbetsammenhengende');
                scope.hvisHarJobbetMerEnn6Uker = function() {
                    return scope.jobbetSammenhengendeFaktum.value == "true";
                }
            }
        };
    });