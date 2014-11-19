module.exports = {
    main: {
        src: [
            '../../views/common/**/*.html',
            '../../views/ettersending/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/ettersending/**/*.html',
            '../../js/common/**/*.html',
            '../../js/modules/**/*.html'
        ],
        dest: '<%= resourcePath %>js/ettersending/templates.js'
    }
};