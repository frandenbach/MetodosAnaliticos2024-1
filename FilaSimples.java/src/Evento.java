public class Evento {
    private double tempo;
    private TipoEvento tipo;

    public Evento(double tempo, TipoEvento tipo) {
        this.tempo = tempo;
        this.tipo = tipo;
    }

    public double getTempo() {
        return tempo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }
}