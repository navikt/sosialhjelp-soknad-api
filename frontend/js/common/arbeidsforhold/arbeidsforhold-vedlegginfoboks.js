angular.module('nav.arbeidsforhold.vedlegginfoboks', [])
    .directive('arbeidsforholdVedleggInfoboks', function () {
    return {
        restrict   : 'A',
        replace    : true,
        scope      : {
            vedleggtekster : "=",
            infotekster    : "="
        },
        templateUrl: '../js/common/arbeidsforhold/templates/arbeidsforholdVedleggInfoboksTemplate.html'
    };
});