angular.module('nav.nowrap', [])
    .directive('nowrap', function ($timeout) {
        return function (scope, element) {
            $timeout(function () {
                var tekst = element.text();
                var sisteOrd = hentSisteOrd(tekst);
                var nyTekst = tekst.substring(0, tekst.indexOf(sisteOrd)).trim() + ' ';
                var nowrapElement = angular.element('<nobr></nobr>')
                nowrapElement.text(sisteOrd);
                nowrapElement.append(angular.element(element.children()[0]));
                element.text(nyTekst);
                element.append(nowrapElement);
            });
        };
    });