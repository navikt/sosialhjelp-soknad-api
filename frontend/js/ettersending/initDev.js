angular.module('ettersending')
    .run(['$http', '$templateCache', function ($http, $templateCache) {
        $http.get('../views/ettersending/nytt-vedlegg.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/modalsideTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/navinfoboksTemplate.html', {cache: $templateCache});
        $http.get('../js/common/directives/markup/panelStandardBelystTemplate.html', {cache: $templateCache});
        $http.get('../views/templates/avbryt.html', {cache: $templateCache});
        $http.get('../views/templates/avbrutt.html', {cache: $templateCache});
        $http.get('../js/modules/avbryt/templates/slettSoknadTemplate.html', {cache: $templateCache});
        $http.get('../js/modules/avbryt/templates/soknadSlettetTemplate.html', {cache: $templateCache});
    }]);