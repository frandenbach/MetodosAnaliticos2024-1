import java.util.PriorityQueue;
import java.util.Random;

public class FilaSimples {
    private Random random = new Random();
    private int M = 100000;
    private int anterior = random.nextInt(M);

    public double nextRandom() {
        anterior = (10 * anterior + 5) % M;
        return (double) anterior / M;
    }

    public double nextChegada(double min, double max) {
        return min + (max - min) * nextRandom();
    }

    public double nextAtendimento(double min, double max) {
        return min + (max - min) * nextRandom();
    }

    public void chegada(double tempo, PriorityQueue<Evento> eventos, int capacidade, int servidores, int[] times, int perdaClientes) {
        double tempoAnterior = !eventos.isEmpty() ? eventos.peek().getTempo() : tempo;
        if (eventos.size() < capacidade) {
            eventos.add(new Evento(tempoAnterior + nextAtendimento(3.0, 5.0), TipoEvento.SAIDA));
            if (eventos.size() <= servidores) {
                eventos.add(new Evento(tempoAnterior + nextChegada(2.0, 5.0), TipoEvento.CHEGADA));
            }
        } else {
            perdaClientes++;
        }
        for (int i = 0; i < times.length; i++) {
            times[i] += eventos.size() == i ? (tempoAnterior - tempo) : 0;
        }
    }

    public void saida(double tempo, PriorityQueue<Evento> eventos, int servidores, int[] times) {
        if (!eventos.isEmpty()){
            Evento evento = eventos.poll();
            double tempoAnterior = evento.getTempo();

            if (!eventos.isEmpty() && eventos.size() >= servidores) {
                eventos.add(new Evento(tempoAnterior + nextAtendimento(3.0, 5.0), TipoEvento.SAIDA));
            }
            for (int i = 0; i < times.length; i++) {
                if (!eventos.isEmpty()) {
                    times[i] += eventos.size() == i ? (tempoAnterior - tempo) : 0;
                } else {
                    times[i] += (tempoAnterior - tempo);
                }
            }
        }
    }
}
