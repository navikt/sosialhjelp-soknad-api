angular.module('nav.cmstekster',['app.services'])
    .directive('cmstekster', ['cms', '$compile', function(cms, $compile) {

        return {
            scope: false,
            link: function (scope, element, attrs) {
                var nokkel = attrs['cmstekster'];
                var cmstekst = cms.tekster[nokkel];

                if (cmstekst === undefined) {
                    return;
                }

                if (element.is('input')) {
                    element.attr('value', cmstekst);
                } else {
                    element.text(cmstekst);

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