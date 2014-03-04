angular.module('nav.bildelastet', [])
    .directive('fetchSpinner', [function() {
        return function(scope, element, attrs) {
            var size = attrs.fetchSpinner !== undefined ? attrs.fetchSpinner : 128;
            var img = angular.element('<img class="spinner vekk" src="../img/ajaxloader/svart/loader_svart_'+ size + '.gif"/>');
            $('body').append(img);
            img.remove();
        };
    }])
    .directive('ferdigLastetBilde', [function () {
        return {
            link: function (scope, element) {
                element.parent().addClass('laster');
                element[0].onload = function() {
                    element.parent().removeClass('laster');
                };
            }
        };
    }]);
