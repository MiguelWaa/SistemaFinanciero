package com.utp.sistemafinanciero.plan;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import static org.junit.Assert.*;

/** Evita reintroducir scripts inline y referencias rotas tras aplicar CSP. */
public class FrontendCspTest {
    @Test public void todasLasPaginasUsanScriptsExternosSinEventosInline() throws Exception {
        Pattern script=Pattern.compile("<script\\b([^>]*)>(.*?)</script\\s*>",Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
        Pattern evento=Pattern.compile("<[^>]+\\son[a-z]+\\s*=",Pattern.CASE_INSENSITIVE);
        Pattern src=Pattern.compile("\\bsrc\\s*=\\s*\"([^\"]+)\"",Pattern.CASE_INSENSITIVE);
        try(var pages=Files.list(Path.of("web"))) {
            for(Path f:pages.filter(p->p.toString().endsWith(".html")).toList()) {
                String html=Files.readString(f);
                assertFalse(f.toString(),evento.matcher(html).find());
                var m=script.matcher(html);
                while(m.find()) {
                    assertTrue("Script inline: "+f,m.group(2).isBlank());
                    var sm=src.matcher(m.group(1)); assertTrue("Sin src: "+f,sm.find());
                    Path archivo=Path.of("web").resolve(sm.group(1)).normalize();
                    assertTrue("Recurso ausente: "+archivo,Files.isRegularFile(archivo));
                }
            }
        }
    }
}
