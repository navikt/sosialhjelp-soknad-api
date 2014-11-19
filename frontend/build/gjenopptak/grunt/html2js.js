module.exports = {
    main: {
        src: [
            '../../views/common/**/*.html',
            '../../views/gjenopptak/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/gjenopptak/**/*.html',
            '../../js/common/**/*.html',
            '../../js/modules/**/*.html'
        ],
        dest: '<%= resourcePath %>js/gjenopptak/templates.js'
    }
};