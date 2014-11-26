module.exports = {
    main: {
        options: {
            base: '../'
        },
        src: [
            '../../views/common/**/*.html',
            '../../views/dagpenger/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/gjenopptak/**/*.html',
            '../../js/common/**/*.html',
            '../../js/modules/**/*.html'
        ],
        dest: '<%= resourcePath %>js/gjenopptak/templates.js'
    }
};