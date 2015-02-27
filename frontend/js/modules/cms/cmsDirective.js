angular.module('nav.cms.directive', [])
    .directive('cmsvedlegg', function () {
        return {
            scope: false,
            required: 'navFaktum',
            link: {
                pre: function (scope, elem, attr) {
                    scope.cmsProps = {};
                    if (attr.cmsvedlegg) {
                        scope.cmsProps.ekstra = attr.cmsvedlegg;
                    }
                }
            }
        };
    });