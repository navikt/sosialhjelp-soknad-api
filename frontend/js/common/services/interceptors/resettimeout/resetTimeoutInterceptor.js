angular.module('nav.services.interceptor.timeout', [])
    .factory('resetTimeoutInterceptor', [function () {
        return {
            'response': function(response) {
                // Bare reset dersom kallet gikk gjennom
                TimeoutBox.startTimeout();
                return response;
            }
        };
    }]);