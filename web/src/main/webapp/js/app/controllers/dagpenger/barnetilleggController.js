angular.module('nav.barnetillegg',[])
.directive('barneDirective', ['$cookieStore', '$timeout', function ($cookieStore, $timeout)  {
	return function($scope) {
		$timeout(function() {
			var barneCookie = $cookieStore.get('barneCookie');
			if(barneCookie) {
				$scope.$emit("CLOSE_TAB", "reell-arbeidssoker");
				$scope.$emit("OPEN_TAB", barneCookie.aapneTabs);
				
            	$timeout(
            		function() {
            				scrollToElement(angular.element("#barnetillegg"),0);
            				console.log(new Date().getTime());
            			}
            		,600);
            	$cookieStore.remove('barneCookie');
			}
		})
	}

}])
.controller('BarnetilleggCtrl', ['$scope', '$cookieStore', '$location', '$timeout', function ($scope, $cookieStore, $location, $timeout) {

	if ($scope.soknadData.fakta.barn) {
		angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
			value.value = angular.fromJson(value.value);
		});
	}       

	$scope.leggTilBarn = function() {
		var aapneTabIds = [];
		angular.forEach($scope.grupper, function(gruppe) {
			if(gruppe.apen) { 
				aapneTabIds.push(gruppe.id); 
			}
		});

		$cookieStore.put('barneCookie', {
			aapneTabs: aapneTabIds,
			gjeldendeTab:"#barnetillegg",
			barneFaktumId: undefined

		})
		$location.path('nyttbarn/' + $scope.soknadData.soknadId);
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

        // For å åpne opp taben. Dataen som blir sendt med eventen er ID på accordion-group som skal åpnes
        //$scope.$emit("OPEN_TAB", 'barnetillegg');

    }]);