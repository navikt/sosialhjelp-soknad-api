angular.module('nav.autoresizetextarea', [])
    .directive('autoresizetextarea', [function () {
        return {
            link: function (scope, element) {
                $(element).on('keyup', 'textarea', function (e) {
                    $(this).css('height', '0px');
                    $(this).height(this.scrollHeight);
                });
                $(element).find('textarea').keyup();
            }
        }
    }]);
