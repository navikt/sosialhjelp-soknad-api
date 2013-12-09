angular.module('nav.barnetilleggfaktum',['app.services'])
    .controller('BarnetilleggFaktumCtrl', ['$scope', 'BrukerData', function ($scope,BrukerData) {
    	var barnetilleggsData = {
    		key: 'barnetillegg',
    		value: {
    			'fnr': $scope.b.value.fnr,
    			'avsjekket': false
    		}
    	};
    	 if ($scope.soknadData.fakta.barnetillegg) {
            angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
                if(angular.fromJson(value.value).fnr == $scope.b.value.fnr) {
                	barnetilleggsData = value;
                	barnetilleggsData.value = angular.fromJson(value.value);
                }
            });
        }

    	$scope.barnetillegg = new BrukerData(barnetilleggsData);

    	$scope.$watch('barnetillegg.value.avsjekket', function(newValue, oldValue, scope) {
    		if(newValue != undefined && newValue !== oldValue) {
    			scope.barnetillegg.$create({soknadId: scope.soknadData.soknadId}).then(function(data) {
    				scope.barnetillegg = data;
    				scope.barnetillegg.value = angular.fromJson(data.value);
    			});
    		}
    	});
    }]);