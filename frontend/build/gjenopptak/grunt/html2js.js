module.exports = {
    main: {
        src: [
            '../../views/common/**/*.html',
            '../../views/gjenopptak/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/gjenopptak/**/*.html',
            '../../js/common/**/*.html'
        ],
        dest: '<%= resourcePath %>js/gjenopptak/templates.js'
    }
};