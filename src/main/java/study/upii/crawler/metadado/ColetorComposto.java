package study.upii.crawler.metadado;

public final class ColetorComposto extends ColetorDeMetadado.Wrapper {
    private final int anuncios;

    public ColetorComposto(ColetorDeMetadado c1, ColetorDeMetadado c2) {
        super(c1.concat(c2));
        anuncios = c1.printer().anuncios() + c2.printer().anuncios();
    }

    private ColetorComposto(ColetorDeMetadado c1, int anuncios) {
        super(c1);
        this.anuncios = anuncios;
    }

    @Override
    public Printer printer() {
        return new Printer.Wrapper(super.printer()) {
            @Override
            public int anuncios() {
                return anuncios;
            }
        };
    }

    @Override
    public ColetorDeMetadado concat(ColetorDeMetadado coletor) {
        return new ColetorComposto(super.concat(coletor), anuncios + coletor.printer().anuncios());
    }
}
