angular.module('nav.tilleggsopplysninger')
    .directive('validerFritekst', [function () {
        return {
            require: '^form',
            link: function (scope, element, attrs, form) {
                var blokk = $(element).closest('.spm-blokk');

                scope.$watch(function () {
                    return form.$valid;
                }, function () {
                    if (form.$valid) {
                        if (blokk.hasClass('open')) {
                            blokk.addClass('validert');
                        }
                    } else {
                        blokk.removeClass('validert');
                    }
                });
            }
        };
    }]);