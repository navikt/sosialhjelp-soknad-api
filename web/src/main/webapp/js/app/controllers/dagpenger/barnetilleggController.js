angular.module('nav.barnetillegg',[])
.controller('BarnetilleggCtrl', ['$scope', '$cookieStore', '$location', '$timeout', 'barneService','BrukerData', function ($scope, $cookieStore, $location, $timeout, barneService, BrukerData) {
	barneService.get({soknadId: $scope.soknadData.soknadId}).$promise.then(function (result) {
		if ($scope.soknadData.fakta.barn) {
			angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
				value.value = angular.fromJson(value.value);
			});
		}       

		$scope.leggTilBarn = function() {
			settBarnCookie();
			$location.path('nyttbarn/' + $scope.soknadData.soknadId);
		}

		$scope.endreBarn = function(faktumId) {
			settBarnCookie(faktumId);
			$location.path('endrebarn/' + $scope.soknadData.soknadId + "/" + faktumId);
		}

		$scope.slettBarn = function(b, index) {
			b.value = angular.toJson(b.value);
			$scope.barnSomSkalSlettes = new BrukerData(b);

			$scope.barnSomSkalSlettes.$delete({soknadId: $scope.soknadData.soknadId}).then(function() {
				$scope.soknadData.fakta.barn.valuelist.splice(index, 1);
			});
		}

		$scope.erGutt = function(barn) {
			return barn.value.kjonn == "gutt";
		}

		$scope.erJente = function(barn) {
			return barn.value.kjonn == "jente";
		}

		$scope.validerBarnetillegg = function(form) {
			$scope.validateForm(form.$invalid);
			$scope.runValidation();
		}

		function settBarnCookie(faktumId) {
			var aapneTabIds = [];
			angular.forEach($scope.grupper, function(gruppe) {
				if(gruppe.apen) { 
					aapneTabIds.push(gruppe.id); 
				}
			});

			$cookieStore.put('barneCookie', {
				aapneTabs: aapneTabIds,
				gjeldendeTab:"#barnetillegg",
				barneFaktumId: faktumId
			})
		}
	})

}]);