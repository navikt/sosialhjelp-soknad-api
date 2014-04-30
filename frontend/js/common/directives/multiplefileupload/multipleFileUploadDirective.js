angular.module('opplasting.multiple', [])
    .directive('multipleUpload', function () {
        return function (scope, element) {
            if (erMobil()) {
                element.removeAttr('multiple');
            }
        };
    });