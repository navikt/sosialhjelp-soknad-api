module.exports = {
    options: {
        singleQuotes: true
    },
    dist: {
        files: [
            {
                expand: true,
                src: ['<%= resourcePath %>/js/built/*.js']
            }
        ]
    }
};