package md.esempla.webflux.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.*;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PdfService {

    public byte[] generatePdfFromTemplate(String templatePath, Map<String, Object> data) throws IOException {
        // Render Mustache template to HTML
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(templatePath);
        StringWriter htmlWriter = new StringWriter();
        mustache.execute(htmlWriter, data).flush();

        // Convert HTML to PDF
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlWriter.toString(), null);
        builder.toStream(pdfStream);
        builder.run();

        return pdfStream.toByteArray();
    }
}
