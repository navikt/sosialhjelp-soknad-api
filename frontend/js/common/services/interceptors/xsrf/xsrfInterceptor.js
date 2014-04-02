angular.module('nav.services.interceptor.xsrf', [])
    .factory('xsrfRelast', ['$q', function ($q) {
        return {
            'responseError': function(response){
                if(response.status === 403){
                    //("Vi må håndtere feil fra rest kall på en god måte. 403 er når xsrf token ikke matcher. Må laste siden på nytt.")
                }
                $q.reject(response);
            }
        };
    }]);