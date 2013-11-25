angular.module('nav.forsettsenere',['nav.cmstekster'])
    .controller('FortsettSenereCtrl', ['$scope', 'soknadService', '$routeParams', '$http', '$location', 
        function ($scope, soknadService, $routeParams, $http,  $location) {

        $scope.forsettSenere = function() {
            var soknadId = $routeParams.soknadId;
			$http.post('/sendsoknad/rest/soknad/' + soknadId +'/fortsettsenere', $scope.soknadData.fakta.epost.value)
				.success(function(data) {
                    $location.path('kvittering-fortsettsenere/' + soknadId);
				});
        }
        
    }])

$scope.validerEpostAdresse = function (emailAddress) {
    return EMAIL_REGEXP.test(emailAddress);
}

    .directive('navGjenoppta', ['$compile','data', function($compile, data) {
        
        var getForDelsteg = function(delstegstatus) {
            var templateUrl = '';
            switch (delstegstatus){
                case "UTFYLLING":
                    templateUrl = "../html/templates/gjenoppta/skjema-under-arbeid.html";
                    break;
                case "VEDLEGG_VALIDERT":
                    templateUrl = "../html/templates/gjenoppta/skjema-ferdig.html";
                    break;

                case "SKJEMA_VALIDERT":
                    templateUrl = "../html/templates/gjenoppta/skjema-validert.html";
                    break;
                default:
                    templateUrl = "../html/templates/gjenoppta/skjema-under-arbeid.html";

            }
            return templateUrl;
        }

        var getTemplateUrl =  function(status, delstegstatus) {
            var templateUrl = '';
            switch (status) {
                    case "UNDER_ARBEID":
                        templateUrl = getForDelsteg(delstegstatus);
                        break;
                    case "FERDIG":
                        templateUrl = "../html/templates/gjenoppta/skjema-sendt.html";
                        break;
                    case "AVBRUTT":
                        break;
                }
                return templateUrl;
        }


        var linker = function(scope,element, attrs){
            return getTemplateUrl(data.soknad.status, data.soknad.delstegStatus);
        }

        return{ 
            restrict: "A",
            replace: true,
            templateUrl: linker
        }
    }])


        