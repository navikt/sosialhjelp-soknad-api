angular.module('nav.cmstekster',['app.services'])
    .directive('cmstekster', ['cms', '$compile', function(cms, $compile) {

        return {
            scope: false,
            link: function (scope, element, attrs) {
                var nokkel = attrs['cmstekster'];
                var cmstekst = cms.tekster[nokkel];
                var hjelpetekstTeller = 0;

                if (cmstekst === undefined) {
                    return;
                }

                var inneholderHjelpetekst = cmstekst.indexOf('<span class="definerer-hjelpetekst">');

                if (element.is('input')) {
                    element.attr('value', cmstekst);
                } else if (inneholderHjelpetekst == -1) {
                    element.text(cmstekst);

                } else {
                    var tekstElement = $('<span/>');
                    var tekst = cmstekst;

                    while(tekst != -1) {
                        tekst = finnHjelpetekster(tekst, tekstElement);
                    }
                    element.append(tekstElement);
                }

                function finnHjelpetekster(tekst, tekstElement) {
                    var startIndex = tekst.indexOf('<span class="definerer-hjelpetekst">');

                    if (startIndex > -1) {
                        var sluttIndex = tekst.indexOf('</span>') + '</span>'.length;
                        var hjelpetekst = angular.element(tekst.substring(startIndex, sluttIndex));

                        // For å finne rett cms-nøkkel til hjelpeteksten
                        hjelpetekst.attr('data-nokkel', nokkel + "." + hjelpetekstTeller);
                        hjelpetekstTeller++;

                        var startTekst = $('<span/>').text(tekst.substring(0, startIndex));
                        var sluttTekst = tekst.substring(sluttIndex, tekst.length);

                        $compile(hjelpetekst)(scope);

                        tekstElement.append(startTekst);
                        tekstElement.append(hjelpetekst);

                        return sluttTekst;
                    }

                    tekstElement.append($('<span/>').text(tekst));
                    return startIndex;
                }
            }
        };
    }])
    .directive('cmshtml', ['cms', function (cms) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmshtml'];
            element.html(cms.tekster[nokkel]);
        };
    }])
    .directive('cmslenketekster', ['cms', function(cms) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmstekster'];
            if (element.is('a')) {
                element.attr('href', cms.tekster[nokkel]);
            }
        };
    }]);