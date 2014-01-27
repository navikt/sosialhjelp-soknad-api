angular.module('nav.forsettsenere', ['nav.cmstekster'])
	.controller('FortsettSenereCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', "fortsettSenereService", "Faktum",
		function ($scope, data, $routeParams, $http, $location, fortsettSenereService, Faktum) {
            var lagretEpost = data.finnFaktum('epost');

            if(lagretEpost) {
            	$scope.epost = data.finnFaktum('epost');	
            } else {
            	var personalia = data.finnFaktum('personalia');
            	$scope.epost = {
            		key: 'epost',
            		value: undefined
            	}
            	$scope.epost.value = personalia.properties.epost;
            	//finn i personaia
            }
            
            $scope.soknadId = data.soknad.soknadId;

            $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];

            $scope.forsettSenere = function (form) {
				$scope.validateForm(form.$invalid);
				$scope.$broadcast('RUN_VALIDATION' + form.$name);

				if (form.$valid) {
					var behandlingId = getBehandlingIdFromUrl();
					if ($scope.epost) {
						$scope.epost = new Faktum($scope.epost);
						$scope.epost.$save({soknadId: data.soknad.soknadId}).then(function (epostData) {
							new fortsettSenereService({epost: $scope.epost.value}).$send({behandlingId: behandlingId}).then(function (data) {
								$location.path('kvittering-fortsettsenere/');
							});
						});
					}
				}
			}
	}])
	.controller('FortsettSenereKvitteringCtrl', ['$scope', 'data', '$routeParams', '$http', '$location', "fortsettSenereService",
		function ($scope, data, $routeParams, $http, $location, fortsettSenereService) {
            $scope.inngangsportenUrl = data.config["soknad.inngangsporten.url"];
            $scope.epost = data.finnFaktum('epost');
	}])

	.directive('navGjenoppta', ['$compile', 'data', function ($compile, data) {

		var getForDelsteg = function (delstegstatus) {
			var templateUrl = '';
			switch (delstegstatus) {
				case 'UTFYLLING':
					templateUrl = '../html/templates/gjenoppta/skjema-under-arbeid.html';
					break;
				case 'VEDLEGG_VALIDERT':
					templateUrl = '../html/templates/gjenoppta/skjema-ferdig.html';
					break;
				case 'SKJEMA_VALIDERT':
					templateUrl = '../html/templates/gjenoppta/skjema-validert.html';
					break;
				default:
					templateUrl = '../html/templates/gjenoppta/skjema-under-arbeid.html';

			}
			return templateUrl;
		};

		var getTemplateUrl = function (status, delstegstatus) {
			var templateUrl = '';
			switch (status) {
				case 'UNDER_ARBEID':
					templateUrl = getForDelsteg(delstegstatus);
					break;
				case 'FERDIG':
					templateUrl = '../html/templates/gjenoppta/skjema-sendt.html';
					break;
				case 'AVBRUTT':
					break;
			}
			return templateUrl;
		};


		var linker = function (scope, element, attrs) {
			return getTemplateUrl(data.soknad.status, data.soknad.delstegStatus);
		};

		return{
			restrict   : 'A',
			replace    : true,
			templateUrl: linker
		}
	}]);


