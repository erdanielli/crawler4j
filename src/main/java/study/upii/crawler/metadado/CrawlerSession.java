package study.upii.crawler.metadado;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class CrawlerSession implements Supplier<ColetorDeMetadado> {
    private final ScheduledExecutorService progressLogExecutor;
    private final Duration interval;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected abstract String[] seeds();

    protected abstract ColetorDeMetadado criarColetorDeMetadado();

    protected abstract MetadadoCrawler criarMetadadoCrawler(ColetorDeMetadado coletor) throws Exception;

    protected int instancias() {
        return Runtime.getRuntime().availableProcessors();
    }

    protected void logProgress() {
    }

    public CrawlerSession(ScheduledExecutorService progressLogExecutor, Duration interval) {
        this.progressLogExecutor = progressLogExecutor;
        this.interval = interval;
    }

    @Override
    public ColetorDeMetadado get() {
        long cron = System.currentTimeMillis();
        CrawlController controller = null;
        ScheduledFuture<?> progressLogger = scheduleProgressLogger();
        try {
            controller = crawlController();
            for (String s : seeds()) {
                controller.addSeed(s);
            }
            List<ColetorDeMetadado> coletores = new ArrayList<>();
            log.info("[SESSION_STARTED]");
            controller.start((CrawlController.WebCrawlerFactory<WebCrawler>) () -> {
                ColetorDeMetadado coletor = criarColetorDeMetadado();
                coletores.add(coletor);
                MetadadoCrawler mc = criarMetadadoCrawler(coletor);
                log.debug("[CRAWLER_CREATED] {}", mc);
                return mc;
            }, instancias());
            log.info("[SESSION_COMPLETED] took {}", Duration.ofMillis(System.currentTimeMillis() - cron));
            return concat(coletores);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("INTERRUPTED", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("FAILED", e);
        } finally {
            if (controller != null) {
                deleteQuietly(controller.getConfig().getCrawlStorageFolder());
            }
            progressLogger.cancel(false);
        }
    }

    private ScheduledFuture<?> scheduleProgressLogger() {
        long seconds = interval.toSeconds();
        return progressLogExecutor.scheduleAtFixedRate(this::logProgress, seconds, seconds, TimeUnit.SECONDS);
    }

    private CrawlController crawlController() throws Exception {
        Path p = Files.createTempDirectory("crawler-session-");
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(p.toAbsolutePath().toString());
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        return new CrawlController(config, pageFetcher, robotstxtServer);
    }

    private ColetorDeMetadado concat(List<ColetorDeMetadado> coletores) {
        ColetorDeMetadado merged = criarColetorDeMetadado();
        for (ColetorDeMetadado c : coletores) {
            merged = merged.concat(c);
        }
        return merged;
    }

    private void deleteQuietly(String folder) {
        try {
            Files.walkFileTree(new File(folder).toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("[CLEANUP_FAILED]", e);
        }
    }
}
