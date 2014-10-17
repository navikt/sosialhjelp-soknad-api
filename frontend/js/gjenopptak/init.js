/* jshint scripturl: true */

angular.module('gjenopptak')
    .value('data', {})
    .value('cms', {})
    .run(['$http', '$rootScope', function ($http, $rootScope) {
        $rootScope.app = {
            laster: true
        };
        $('#hoykontrast a, .skriftstorrelse a').attr('href', 'javascript:void(0)');
    }]);