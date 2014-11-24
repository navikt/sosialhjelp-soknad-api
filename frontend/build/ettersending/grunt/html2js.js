module.exports = {
    main: {
        options: {
            base: '../'
        },
        src: [
            '../../views/common/**/*.html',
            '../../views/ettersending/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/ettersending/**/*.html',
            '../../js/common/**/*.html'
        ],
        dest: '<%= resourcePath %>js/ettersending/templates.js'
    }
};