angular.module('nav.forsettsenere',['nav.cmstekster'])
    .controller('FortsettSenereCtrl', ['$scope', 'soknadService', '$routeParams', '$http', '$location', 
        function ($scope, soknadService, $routeParams, $http,  $location) {

        $scope.forsettSenere = function() {
            var soknadId = $routeParams.soknadId;
			$http.post('/sendsoknad/rest/soknad/' + soknadId +'/fortsettsenere', $scope.soknadData.fakta.personalia.epost.value)
				.success(function(data) {
                    $location.path('kvittering-fortsettsenere/' + soknadId);
				});
        }
        
    }])

    .directive('navGjenoppta', ['$compile','data', function($compile, data) {
        
        var getForDelsteg = function(delstegstatus) {
            var t = '';
            switch (delstegstatus){
                case "UTFYLLING":
                    console.log("utfylling");
                    t = "../html/templates/gjenoppta/skjema-under-arbeid.html";
                    break;
                case "FERDIG":
                    t = "../html/templates/gjenoppta/skjema-ferdig.html";
                    break;
            }
            return t;
        }

        var getTemplateUrl =  function(status, delstegstatus) {
            var templateUrl = '';
            switch (status) {
                    case "UNDER_ARBEID":
                        console.log("underarbeids " + delstegstatus);
                        templateUrl = getForDelsteg(delstegstatus);
                    case "SENDT":
                        break;
                    case "AVBRUTT":
                        break;
                }
                return templateUrl;
        }


        var linker = function(scope,element, attrs){
            return getTemplateUrl(data.soknad.status, data.soknad.delstegstatus);
        }

        return{ 
            restrict: "A",
            replace: true,
            templateUrl: linker
        }
    }])


        