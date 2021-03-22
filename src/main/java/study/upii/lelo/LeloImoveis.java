package study.upii.lelo;

import study.upii.Imobiliaria;
import study.upii.Imovel;
import study.upii.crawler.Crawler;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

public final class LeloImoveis implements Imobiliaria {
    private final ConcurrentLinkedDeque<Imovel> imoveis;

    public LeloImoveis(ExecutorService executorService) {
        imoveis = new ConcurrentLinkedDeque<>();
        executorService.submit(new Crawler(() -> new LeloImoveisCrawler(imoveis::addLast),
                "https://leloimoveis.com.br/imoveis/venda",
                "https://leloimoveis.com.br/imoveis/locacao"));
    }

    @Override
    public Iterable<Imovel> imoveisDisponiveis() {
        return imoveis;
    }
}
