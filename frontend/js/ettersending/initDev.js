angular.module('ettersending')
    .run(['$http', '$templateCache', function ($http, $templateCache) {
        $http.get('../views/ettersending/nytt-vedlegg.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/modalsideTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/navinfoboksTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/panelStandardBelystTemplate.html', {cache: $templateCache});
    }]);