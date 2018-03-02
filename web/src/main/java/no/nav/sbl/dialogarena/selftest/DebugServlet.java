package no.nav.sbl.dialogarena.selftest;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DebugServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /*
        final PrintWriter writer = response.getWriter();
        
        writer.println("Environment variables:");
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            writer.println(entry.getKey() + ": " + entry.getValue());
        }
        
        writer.println();
        writer.println();
        
        writer.println("Properties:");
        System.getProperties().store(writer, "UTF-8");
        */
    }
    
}
