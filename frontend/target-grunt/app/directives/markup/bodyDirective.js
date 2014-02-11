angular.module('nav.markup.bodydirective', [])
/**
 * Legges på toppnoden til angular data-ng-view. Sjekker om den finner .modalboks, dersom den finnes settes custom stylesheet.
 **/
    .directive('modalsideHelper', function ($timeout) {
        return function (scope, element) {
            $timeout(function () {
                if (element.find('.modalBoks').length > 0) {
                    $('body').addClass('modalside');
                } else {
                    $('body').removeClass('modalside');
                }
            });
        }
    })
    .directive('avbrytHelper', function ($timeout) {
        return function (scope, element) {
            $timeout(function () {
                if (element.find('.avbryt-boks').length > 0) {
                    $('body').attr('id', 'avbryt-side');
                } else {
                    $('body').attr('id', '');
                }
            });
        }
    });
