package study.upii.crawler.metadado.imobiliaria;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import study.upii.crawler.metadado.ColetorDeMetadado;
import study.upii.crawler.metadado.ColetorDeMetadadoSimples;
import study.upii.crawler.metadado.CrawlerSession;
import study.upii.crawler.metadado.MetadadoCrawler;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Lelo extends CrawlerSession {
    private final Pattern skip = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz))(\\?c=\\d+)?$");
    private final Pattern urlPattern = Pattern.compile("https://leloimoveis\\.com\\.br/imovel/(.*)");
    private final Pattern refPattern = Pattern.compile("ref=([^&]+)");
    private final ConcurrentHashMap<String, Byte> refs = new ConcurrentHashMap<>();

    public Lelo(ScheduledExecutorService progressLogExecutor) {
        this(progressLogExecutor, Duration.ofSeconds(10L));
    }

    public Lelo(ScheduledExecutorService progressLogExecutor, Duration interval) {
        super(progressLogExecutor, interval);
    }

    @Override
    protected ColetorDeMetadado criarColetorDeMetadado() {
        return new ColetorImpl();
    }

    @Override
    protected String[] seeds() {
        Stream<String> locacao = Stream.concat(
                Stream.of("https://leloimoveis.com.br/imoveis/locacao"),
                IntStream.rangeClosed(2, 53)
                        .mapToObj(i -> "-pagina-" + i)
                        .map(s -> "https://leloimoveis.com.br/imoveis/locacao" + s));
        Stream<String> venda = Stream.concat(
                Stream.of("https://leloimoveis.com.br/imoveis/venda"),
                IntStream.rangeClosed(2, 56)
                        .mapToObj(i -> "-pagina-" + i)
                        .map(s -> "https://leloimoveis.com.br/imoveis/venda" + s));
        return Stream.concat(locacao, venda).toArray(String[]::new);
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
            if (!skip.matcher(href).matches() && !href.endsWith("search=0") && urlPattern.matcher(href).matches()) {
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
                coletor.tipo(html.selectFirst("strong.card__property").text().trim());
                for (Element descOrCaract : html.getElementsByClass("card__cps-title")) {
                    for (Element pair : descOrCaract.parent().getElementsByClass("card__cps-item")) {
                        String k = pair.getElementsByClass("card__cps-label").text();
                        String v = pair.getElementsByClass("card__cps-value").text();
                        // remover ':' do final da chave
                        coletor.metadado(k.substring(0, k.length() - 1), v);
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
