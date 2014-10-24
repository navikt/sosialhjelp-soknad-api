module.exports = {
    main: {
        src: [
            '../../views/dagpenger/**/*.html',
            '../../views/common/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/dagpenger/**/*.html',
            '../../js/common/**/*.html'
        ],
        dest: '<%= resourcePath %>js/dagpenger/templates.js'
    }
};