angular.module('nav.fokus', [])
    .directive('fokus', [function () {
        return {
            replace: true,

            link: function (scope, elm) {
                elm.bind("click", function() {
                    settFokusTilNesteElement(elm);
                })
            }
        };
    }]);
