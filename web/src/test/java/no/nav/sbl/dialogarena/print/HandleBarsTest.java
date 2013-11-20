package no.nav.sbl.dialogarena.print;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.lowagie.text.DocumentException;

import java.io.IOException;
import java.util.Map;


public class HandleBarsTest {


    public static void tryit() {
        Handlebars handlebars = new Handlebars();

        try {
            Template template = handlebars.compileInline("Hello {{this}}!");

            System.out.println(template.apply("Hoppeti"));
            System.out.println();
        } catch (IOException e) {

        }
    }

    public static void runHTML() {

//        Handlebars handlebars = new Handlebars();
//        Template template = getTemplate(getGlossaryHtml(), handlebars);
//        try {
//            System.out.println(template.apply("Handlebars.java"));
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }

    public static void runMeWithJson() throws IOException {
//        String json = getJson();
//        Map<String, Object> result = new ObjectMapper().readValue(json, HashMap.class);
//
//        System.out.println("result.keySet() = " + result.keySet());
//        System.out.println("result.values() = " + result.values());
//
//        String applied = apply(getHtml(), new Handlebars(), result);
//        System.out.println("applied = " + applied);

    }

    public static void createPDFFromJson() throws IOException, DocumentException {
//
//        String pdf = "/Users/karibergschjonsby/Documents/dev/test/handlebar.pdf";
//        String baseUrl = "/Users/karibergschjonsby/Documents/dev/test/";
//        String html = HandleBarRunner.getHTML(getJson(), getHtml());
//        System.out.println("html = " + html);
//        PDFCreator.createPDF(html, pdf, baseUrl);
//
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PDFCreator.createPDF(html, baseUrl, out);
//
//        String s = out.toString("utf-8");
//        System.out.println("s = " + s);
//        out.close();

    }

//    public static String getJson() throws IOException {
//        Handlebars handlebars = new Handlebars();
//        handlebars.registerHelper("@json", Jackson2Helper.INSTANCE);
//
//        Template template = handlebars.compileInline("{{@json this}}");
//
//        CharSequence result = template.apply(new Blog("First Post", "..."));
//
//        System.out.println("result = " + result);
//        return result.toString();
//    }

    public static Template getTemplate(String input, Handlebars handlebars) {
        Template template = null;
        try {
            template = handlebars.compileInline(input);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return template;
    }

    public static String apply(String input, Handlebars handlebars, Map<String, Object> hash) {
        String template = null;
        try {
            template = handlebars.compileInline(input).apply(hash);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return template;
    }



}
