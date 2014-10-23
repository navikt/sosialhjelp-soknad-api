module.exports = {
    main: {
        src: [
            'views/common/**/*.html',
            'views/gjenopptak/**/*.html',
            'views/templates/**/*.html',
            'js/gjenopptak/**/*.html',
            'js/common/**/*.html'
        ],
        dest: 'target/classes/META-INF/resources/js/gjenopptak/templates.js'
    }
};