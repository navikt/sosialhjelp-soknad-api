module.exports = {
    main: {
        options: {
            base: '../'
        },
        src: [
            '../../views/dagpenger/**/*.html',
            '../../views/common/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/dagpenger/**/*.html',
            '../../js/common/**/*.html',
            '../../js/modules/**/*.html'
        ],
        dest: '<%= resourcePath %>js/dagpenger/templates.js'
    }
};