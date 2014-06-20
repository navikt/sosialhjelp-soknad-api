angular.module('nav.services.interceptor.feilhandtering', [])
    .factory('feilhandteringInterceptor', ['$q', '$location', '$injector', function ($q, $location, $injector) {
        function visStandardFeilmodal(response) {
            if (response.status === 404 && !response.data) {
                // Kunne ikke koble til server
                return true;
            } else if (response.status === 403) {
                // Feil med XSRF-token
                return true;
            } else if (response.status >= 500) {
                // Andre feil
                return true;
            }

            return false;
        }

        function visOpprettFeilmodal(response) {
            if (response.config.url.indexOf('/soknad/opprett') > -1) {
                // Kunne ikke opprette søknad
                return true;
            }
            return false;
        }

        function innsendingFeilet(response) {
            if (response.config.url.indexOf('/soknad/send') > -1) {
                // Kunne ikke sende inn søknad
                return true;
            }
            return false;
        }

        return {
            'responseError': function (response) {
                if (response === undefined || response.config === undefined) {
                    return $q.reject(response);
                }

                /*
                 * $http har dependency til denne interceptoren, mens denne interceptoren har dependency til $modal.
                 * $modal bruker $http for å hente templates, og har dermed dependency til $http, som gir circular dependencies.
                 * Må derfor "lure" angular med å injecte $modal manuelt inn i interceptoren.
                 *
                 * Alle templates skal forøvrig være prefetchet, så det skal ikke utgjøre noe problem, siden $modal i praksis
                 * aldri skal gjøre kall mot serveren.
                 */
                var $modal = $injector.get('$modal');

                if (visOpprettFeilmodal(response)) {
                    $modal.open({
                        templateUrl: '../views/common/feilsider/opprettSoknadFeilet.html',
                        backdrop: 'static'
                    });
                } else if (innsendingFeilet(response)) {
                    return $q.reject(response); // Sendes videre for å håndteres i oppsummeringscontrolleren
                } else if (visStandardFeilmodal(response)) {
                    $modal.open({
                        templateUrl: '../views/common/feilsider/serverfeil.html',
                        backdrop: 'static'
                    });
                }

                return $q.reject(response);
            }
        };
    }])
    .controller('ServerFeilModalVinduCtrl', ['$scope', 'data', '$filter',  function ($scope, data) {
        $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
        $scope.loggutUrl = $('.innstillinger-innlogget .innlogging .loggut').attr('href');

        $scope.relast = function() {
            window.location.reload();
        };
    }])
    .controller('ServerFeilCtrl', ['$scope', 'data', '$filter', function ($scope, data, $filter) {
        if (data.soknad && data.soknad.sistLagret) {
            $scope.lagretTidspunkt = $filter('date')(data.soknad.sistLagret, 'short');
        }

        $scope.harSistLagretDato = function () {
            return $scope.lagretTidspunkt !== undefined;
        };

        $scope.harIkeSistLagretDato = function () {
            return !$scope.harSistLagretDato();
        };
    }]);