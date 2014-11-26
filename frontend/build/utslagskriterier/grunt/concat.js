module.exports = {
    options: {
        separator: ';'
    },
    dist: {
        src   : [
            '../../js/lib/angular/angular.js',
            '../../js/lib/angular/angular-*.js',
            '../../js/lib/jquery/jquery-ui-1.10.3.custom.js',
            '../../js/lib/jquery/cors/jquery.xdr-transport.js',
            '../../js/lib/jquery/cors/jquery.postmessage-transport.js',
            '../../js/lib/jquery/jquery.iframe-transport.js',
            '../../js/lib/jquery/jquery.fileupload.js',
            '../../js/lib/jquery/jquery.fileupload-process.js',
            '../../js/lib/jquery/jquery.fileupload-validate.js',
            '../../js/lib/jquery/jquery.fileupload-angular.js',
            '../../js/lib/*.js',
            '<%= resourcePath %>js/utslagskriterier/templates.js',
            '../../js/utslagskriterier/**/!(initDev).js',
            '../../js/common/**/!(templates).js',
            '../../js/modules/**/*.js'
        ],
        dest: '<%= resourcePath %><%= jsBuilt %>',
        nonull: true
    }
};