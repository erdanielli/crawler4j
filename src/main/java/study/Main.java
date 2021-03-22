package study;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import study.upii.Imovel;
import study.upii.lelo.LeloImoveis;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@QuarkusMain
public class Main implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(Main.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        LeloImoveis leloImoveis = new LeloImoveis(executorService);

        for (int i = 0; i < 10; i++) {
            int n = 0;
            for (Imovel imovel : leloImoveis.imoveisDisponiveis()) {
                System.out.printf("-----%n#%-3d %s%n     %s%n", ++n, imovel.anuncio(), imovel.url());
                if (!imovel.mapsUrl().isBlank()) {
                    System.out.printf("     %s%n", imovel.mapsUrl());
                }
                if (imovel.imagens().isEmpty()) {
                    System.out.printf("     %s%n", "(nenhuma imagem disponível)");
                } else {
                    for (String img : imovel.imagens()) {
                        System.out.printf("     %s%n", img);
                    }
                }
            }
            System.out.println("===================");
            Thread.sleep(5000L);
        }
        executorService.shutdown();
        return executorService.awaitTermination(10, TimeUnit.SECONDS) ? 0 : -1;
    }
}
