angular.module('nav.services.popup', [])
    .factory('popup', ['$modal', function ($modal) {
        return {
            'openPopup': function () {
                $modal.open({
                    templateUrl: '../js/common/popup/popupTemplate.html'
                });
            }
        };
    }]);
