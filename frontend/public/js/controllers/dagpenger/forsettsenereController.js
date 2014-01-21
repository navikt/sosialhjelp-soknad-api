angular.module('nav.forsettsenere',['nav.cmstekster'])
    .controller('FortsettSenereCtrl', ['$scope', '$routeParams', '$http', '$location', 'forsettSenereService',

        function ($scope, $routeParams, $http, $location, forsettSenereService) {
            $scope.forsettSenere = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.$broadcast('RUN_VALIDATION' + form.$name);
            if(form.$valid) {
                var soknadId = $routeParams.soknadId;
                if($scope.soknadData.fakta.epost) {
                    new forsettSenereService({epost: $scope.soknadData.fakta.epost.value}).$send({soknadId: soknadId}).then(function (data) {
                        $location.path('kvittering-fortsettsenere/' + soknadId);
    				});
                }
            }
        }
    }])

    .directive('navGjenoppta', ['$compile','data', function($compile, data) {
        


        var getTemplateUrl =  function(status, delstegstatus) {
            var templateUrl = '';
            switch (status) {
                    case 'UNDER_ARBEID':
                        templateUrl = '/html/dagpenger.html';
                        break;
                    case 'FERDIG':
                        templateUrl = '/html/dagpenger.html';
                        break;
                    case 'AVBRUTT':
                        break;
                }
                return templateUrl;
        };


        var linker = function(scope,element, attrs){
            return getTemplateUrl(data.soknad.status, data.soknad.delstegStatus);
        };

        return{ 
            restrict: 'A',
            replace: true,
            templateUrl: linker
        }
    }]);


