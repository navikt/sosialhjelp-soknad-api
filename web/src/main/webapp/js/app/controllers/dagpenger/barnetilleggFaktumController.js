angular.module('nav.barnetilleggfaktum',['app.services'])
    .controller('BarnetilleggFaktumCtrl', ['$scope', 'BrukerData', function ($scope,BrukerData) {
    	var barnetilleggsData = {
    		key: 'barnetillegg',
    		value: false,
    		parrentFaktum: $scope.b.faktumId
    	};
    	if ($scope.soknadData.fakta.barnetillegg) {
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
                if(value.parrentFaktum == $scope.b.faktumId) {
                	barnetilleggsData = value;
                }
            });
        }

    	$scope.barnetillegg = new BrukerData(barnetilleggsData);

    	$scope.$watch('barnetillegg.value', function(newValue, oldValue, scope) {
    		if(newValue != undefined && newValue !== oldValue) {
    			scope.barnetillegg.$create({soknadId: scope.soknadData.soknadId}).then(function(data) {
    				scope.barnetillegg = data;
    			});
    		}
    	});
    }]);