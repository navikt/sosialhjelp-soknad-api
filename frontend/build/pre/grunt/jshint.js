module.exports = {
    files  : [
        'gruntfile.js',
        'build/**/*.js',
        'js/**/*.js',
        'test/**/*.js'
    ],
    options: {
        ignores: [
            'js/built/*.js',
            'js/lib/**/*.js',
            'js/dagpenger/templates.js',
            'js/ettersending/templates.js',
            'js/gjenopptak/templates.js',
            'js/utslagskriterier/dagpenger/templates.js',
            'test/karma/lib/angular-mocks.js',
            'js/common/tredjeparts/**/*.js'
        ],
        globals: {
            it: true,
            expect: true,
            describe: true,
            beforeEach: true,
            inject: true,
            angular: true,
            module: true,
            Date: true
        }
    }
};

