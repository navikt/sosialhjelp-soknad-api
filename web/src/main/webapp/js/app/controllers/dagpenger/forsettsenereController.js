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
        
        var getTemplateUrl =  function(status) {
            var templateUrl = '';
            switch (status) {
                    case "UNDER_ARBEID":
                        templateUrl =  "../html/templates/gjenoppta/skjema-under-arbeid.html";
                        break;
                    case "FERDIG":
                        templateUrl =  "../html/templates/gjenoppta/skjema-ferdig.html";
                        break;
                    case "VALIDERT":
                        break;
                    case "VALIDERT_VEDLEGG":
                        break;
                }
                return templateUrl;
        }


        var linker = function(scope,element, attrs){
            return getTemplateUrl(data.soknad.status);
        }

        return{ 
            restrict: "A",
            replace: true,
            templateUrl: linker
        }
    }])


        