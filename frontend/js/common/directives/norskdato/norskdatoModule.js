angular.module('nav.norskDatoFilter', [])
    .filter('norskdato', [function () {
    return function (input) {
        var monthNames = ['januar', 'februar', 'mars', 'april', 'mai', 'juni', 'juli', 'august', 'september', 'oktober', 'november', 'desember'];
        if (input) {
            var dag = input.substring(0, 2);
            var mnd = input.substring(3, 5);
            var year = input.substring(6, 10);
            return dag + '. ' + monthNames[mnd - 1] + ' ' + year;
        }
        return input;
    };
}]);