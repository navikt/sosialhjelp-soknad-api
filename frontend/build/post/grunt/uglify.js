module.exports = {
    options: {
        banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n',
        mangle: true
    },
    my_target: {
        files: [{
            expand: true,
            cwd: '<%= resourcePath %>',
            src: 'js/built/*.js',
            dest: '<%= resourcePath %>',
            ext: '.min.js'
        }]
    }
};