angular.module('nav.services.interceptor.feilhandtering', [])
    .factory('feilhandteringInterceptor', ['$q', '$location', function ($q, $location) {
        return {
            responseError: function(response){
                console.log("Feil response", response);

                if (response.status === 404 && !response.data) { // Kunne ikke koble til server
                    $location.path('#/serverfeil');
                }

                $q.reject(response);
            }
        };
    }]);