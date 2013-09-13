angular.module('brukerdata', ['services'])

.controller('SoknadDataCtrl', ['$scope', 'soknadService', function($scope, soknadService) {

	$scope.hentSoknadData = function(soknadId) {
		$scope.soknadData = soknadService.get({id: soknadId});
	}

	$scope.leggTil = function(soknadId, key, value) {
		$scope.soknadData.fakta[key] = {"soknadId":soknadId,"key": key,"value": value};
		var soknadData = $scope.soknadData;
		soknadData.$save();
	};
}])

/*
Eksempel som viser en get ved bruk av $http. Kan brukes om man ønsker bedre kontroll
*/
/*
function SoknadDataCtrl_http($scope, $http) {
	$http({method: 'GET', url: '/sendsoknad/rest/soknad/' + 1}).
		success(function (data, status) {
			$scope.soknadData = data
		}).
		error(function(data, status){
			alert("En feil skjedde");
		});
}*/

