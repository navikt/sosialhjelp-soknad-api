angular.module('nav.arbeidsforhold.vedlegginfoboks', [])
    .directive('arbeidsforholdVedleggInfoboks', function ($parse) {
    return {
        restrict   : 'A',
        replace    : true,
        templateUrl: '../js/common/arbeidsforhold/templates/arbeidsforholdVedleggInfoboksTemplate.html',
        link: function(scope, element, attrs) {
            scope.vedleggTekster = $parse(attrs.vedleggtekster)(scope);
            scope.infoTekster = $parse(attrs.infotekster)(scope);
        }
    };
});