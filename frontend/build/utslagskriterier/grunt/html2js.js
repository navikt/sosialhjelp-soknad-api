module.exports = {
    main: {
        src: [
            '../../views/common/**/*.html',
            '../../views/utslagskriterier/**/*.html',
            '../../views/templates/**/*.html',
            '../../js/utslagskriterier/**/*.html',
            '../../js/common/**/*.html'
        ],
        dest: '<%= resourcePath %>js/utslagskriterier/templates.js'
    }
};