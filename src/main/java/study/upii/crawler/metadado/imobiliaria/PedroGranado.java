package study.upii.crawler.metadado.imobiliaria;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import study.upii.crawler.metadado.ColetorDeMetadado;
import study.upii.crawler.metadado.ColetorDeMetadadoSimples;
import study.upii.crawler.metadado.CrawlerSession;
import study.upii.crawler.metadado.MetadadoCrawler;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.IntStream.rangeClosed;

public final class PedroGranado extends CrawlerSession {
    private final Pattern skip = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))(\\?c=\\d+)?$");
    private final Pattern urlPattern = Pattern.compile("https://www\\.pedrogranado\\.com\\.br/(alugar|comprar|lancamentos)/(.*)");
    private final Pattern refPattern = Pattern.compile(".*/(\\d+)$");
    private final ConcurrentHashMap<String, Byte> refs = new ConcurrentHashMap<>();

    public PedroGranado(ScheduledExecutorService progressLogExecutor) {
        this(progressLogExecutor, Duration.ofSeconds(10L));
    }

    public PedroGranado(ScheduledExecutorService progressLogExecutor, Duration interval) {
        super(progressLogExecutor, interval);
    }

    @Override
    protected ColetorDeMetadado criarColetorDeMetadado() {
        return new ColetorImpl();
    }

    @Override
    protected String[] seeds() {
        return Stream.concat(
                rangeClosed(1, 33)
                        .mapToObj(i -> "https://www.pedrogranado.com.br/comprar/locacao_venda=V&cidade=&tipo_mae=&tipo_imovel=&bairro=&nova_paginacao=1&&pag=" + i),
                rangeClosed(1, 37)
                        .mapToObj(i -> "https://www.pedrogranado.com.br/alugar/locacao_venda=L&cidade=&tipo_mae=&tipo_imovel=&bairro=&nova_paginacao=1&&pag=" + i)
        ).toArray(String[]::new);
    }

    @Override
    protected MetadadoCrawler criarMetadadoCrawler(ColetorDeMetadado coletor) throws Exception {
        return new CrawlerImpl(coletor);
    }

    @Override
    protected int instancias() {
        return 4;
    }

    @Override
    protected void logProgress() {
        log.info("{} anuncios processados", refs.size());
    }

    private class CrawlerImpl extends MetadadoCrawler {

        CrawlerImpl(ColetorDeMetadado coletor) {
            super(coletor);
        }

        @Override
        public boolean shouldVisit(Page referringPage, WebURL url) {
            String href = url.getURL().toLowerCase();
            if (!skip.matcher(href).matches() && urlPattern.matcher(href).matches()) {
                Matcher m = refPattern.matcher(href);
                if (m.find()) {
                    String ref = m.group(1);
                    return refs.putIfAbsent(ref, (byte) 0) == null;
                }
            }
            return false;
        }

        @Override
        protected void visitHtml(Document html, Page page, ColetorDeMetadado coletor) {
            try {
                Elements list = html.select("div.comodos");
                for (Element el : list) {
                    String v = el.text();
                    String k = el.nextElementSibling().text();
                    coletor.metadado(k, v);
                }

                list = html.getElementsByTag("h3");
                for (Element el : list) {
                    if (el.text().equals("Características") || el.text().equals("Itens do imóvel")) {
                        Elements labels = el.parent().select("div.row strong");
                        for (Element label : labels) {
                            String t = label.text();
                            if (t.equals("Categoria:")) {
                                coletor.tipo(label.parent().getElementsByTag("span").text());
                            } else {
                                if (t.charAt(t.length() - 1) == ':') {
                                    t = t.substring(0, t.length() - 1);
                                }
                                coletor.metadado(t, label.parent().getElementsByTag("span").text());
                            }
                        }
                    }
                }

            } catch (RuntimeException e) {
                log.warn("[PAGE_SKIPPED] {}", page.getWebURL().getURL());
            }
        }
    }

    private class ColetorImpl extends ColetorDeMetadado.Wrapper {
        ColetorImpl() {
            this(new ColetorDeMetadadoSimples());
        }

        private ColetorImpl(ColetorDeMetadado impl) {
            super(impl);
        }



        @Override
        public Printer printer() {
            return new Printer.Wrapper(super.printer()) {
                @Override
                public int anuncios() {
                    return refs.size();
                }
            };
        }

        @Override
        public ColetorDeMetadado concat(ColetorDeMetadado coletor) {
            return new ColetorImpl(super.concat(coletor));
        }
    }
}
