module.exports = {
    options: {
        banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n',
        mangle: true
    },
    my_target: {
        files: {
            '<%= resourcePath %><%= jsMin %>': ['<%= resourcePath %><%= jsBuilt %>']
        }
    }
};