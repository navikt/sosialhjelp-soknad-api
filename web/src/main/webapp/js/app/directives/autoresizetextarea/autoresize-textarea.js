angular.module('nav.autoresizetextarea', [])
    .directive('autoresizetextarea', [function () {
        return {
            link: function (scope, element, attrs) {
                $('#frivillig-textarea').on('keyup', 'textarea', function (e) {
                    $(this).css('height', '0px');
                    $(this).height(this.scrollHeight);
                });
                $('#frivillig-textarea').find('textarea').keyup();
            }
        }
    }]);
