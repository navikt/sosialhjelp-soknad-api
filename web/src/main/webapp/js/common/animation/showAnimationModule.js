angular.module('nav.animation', [])
    .animation('.animate-slide-show', [function() {
        return {
            addClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    element.slideUp();
                } else {
                    done();
                }
            },
            removeClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    element.removeClass('ng-hide');
                    element.slideDown();
                } else {
                    done();
                }
            }
        }
    }]);