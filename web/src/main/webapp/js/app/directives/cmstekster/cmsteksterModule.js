angular.module('nav.cmstekster',['app.services'])
    .directive('cmstekster', ['data', '$compile', function(data, $compile) {

        return {
            scope: false,
            link: function (scope, element, attrs) {
                var nokkel = attrs['cmstekster'];
                var cmstekst = data.tekster[nokkel];

                if (cmstekst === undefined) {
                    return;
                }

                var inneholderHjelpetekst = cmstekst.indexOf('<span class="definerer-hjelpetekst">');

                if (element.is('input')) {
                    element.attr('value', cmstekst);
                } else if (inneholderHjelpetekst > -1) {
                    var tekstElement = $('<span/>');
                    var tekst = cmstekst;
                    while(tekst != -1) {
                        tekst = finnHjelpetekster(tekst, tekstElement);
                    }
                    element.append(tekstElement);
                } else {
                    element.text(cmstekst);
                }

                function finnHjelpetekster(tekst, tekstElement) {
                    var startIndex = tekst.indexOf('<span class="definerer-hjelpetekst">');

                    if (startIndex > -1) {
                        var sluttIndex = tekst.indexOf('</span>') + 7;
                        var hjelpetekst = angular.element(tekst.substring(startIndex, sluttIndex));
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
    .directive('cmshtml', ['data', function (data) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmshtml'];
            element.html(data.tekster[nokkel]);
        };
    }])
    .directive('cmslenketekster', ['data', function(data) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmstekster'];
            if (element.is('a')) {
                element.attr('href', data.tekster[nokkel]);
            }
        };
    }]);