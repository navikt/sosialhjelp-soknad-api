angular.module('app.brukerdata', ['app.services'])

.controller('StartSoknadCtrl', function($scope, $location, soknadService) {
	$scope.startSoknad = function() {
		var soknadType = window.location.pathname.split("/")[3];
		$scope.soknad = soknadService.create({param: soknadType}).$promise.then(function(result) {
            $location.path('reell-arbeidssoker/' + result.id);
		});
	}
})

.controller('SendSoknadCtrl', function($scope, $routeParams, soknadService) {
    $scope.sendSoknad = function() {
        soknadService.send({param: soknadId, action: 'send'});
    }
})

.controller('HentSoknadDataCtrl', function($scope, $rootScope, $routeParams, soknadService){
    var soknadId = $routeParams.soknadId;
    $scope.soknadData = soknadService.get({param:  soknadId});
})

.controller('SoknadDataCtrl', function($scope, $routeParams, $location, $timeout, soknadService) {
	$scope.soknadData = soknadService.get({param:  $routeParams.soknadId});

	$scope.lagre = function() {

		var soknadData = $scope.soknadData;
		console.log("lagre: " + soknadData);
		soknadData.$save({param: soknadData.soknadId});
	}

	$scope.change = function() {
        console.log("Hei");
	}
	
	/*
	function lagre() {
		$timeout(function() {
			var soknadData = $scope.soknadData;
			soknadData.$save({id: soknadData.soknadId});
			lagre();
		}, 60000);
	}
	lagre();
	*/

})

.controller('AvbrytCtrl', function($scope, $rootScope, $routeParams, $location, soknadService) {

    $scope.data = {krevBekreftelse: $rootScope.soknadPaabegynt};

    $scope.submitForm = function() {
        var start = $.now();
        soknadService.delete({param: $routeParams.soknadId}).$promise.then(function() {

            // For å forhindre at lasteindikatoren forsvinner med en gang
            var delay = 1500 - ($.now() - start);
            setTimeout(function() {
                $scope.$apply(function() {
                    $location.path('slettet');
                });
            }, delay);
        });
    }

    if (!$scope.data.krevBekreftelse) {
        $scope.submitForm();
    }
})

.directive('modFaktum', function() {
	return function($scope, element, attrs) {
	    var eventType;
	    switch(element.attr('type')) {
            case "radio":
            case "checkbox":
                eventType = "change";
                break;
            default:
                eventType = "blur";
	    }

		element.bind(eventType, function() {
			$scope.soknadData.fakta[attrs.name] = {"soknadId": $scope.soknadData.soknadId, "key":attrs.name,"value":element.val()};
			$scope.$apply();
			$scope.lagre();
		});
	};
})


.factory('time', function($timeout) {
	var time = {};

	(function tick() {
		time.now = new Date().toString();
		$timeout(tick, 1000);
	})();
	return time;
}) 

.controller('SistLagretCtrl', function($scope, time) {
	$scope.time = time;
})

