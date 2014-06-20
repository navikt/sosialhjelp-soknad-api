angular.module('nav.modal.controller', [])
    .controller('NavModalCtrl', ['$scope', '$modalStack', function($scope, $modalStack) {
        $scope.lukk = function() {
            $modalStack.getTop().key.dismiss('cancel')
        };
    }]);