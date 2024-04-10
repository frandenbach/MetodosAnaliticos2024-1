import java.util.PriorityQueue;

public class Main {
    public static void main(String[] args) {
        int capacidade = 5;
        int servidores = 1;
        int count = 100000;
        double tempoGlobal = 0.0;
        int perdaClientes = 0;

        int[] times = new int[capacidade + 1];

        PriorityQueue<Evento> eventos = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTempo(), e2.getTempo()));

        eventos.add(new Evento(2.0, TipoEvento.CHEGADA));

        FilaSimples fila = new FilaSimples();

        while (count > 0 && !eventos.isEmpty()) {
            Evento evento = eventos.poll();
            double tempo = evento.getTempo();

            if (evento.getTipo() == TipoEvento.CHEGADA) {
                fila.chegada(tempo, eventos, capacidade, servidores, times, perdaClientes);
                eventos.add(new Evento(tempo + fila.nextChegada(2.0, 5.0), TipoEvento.CHEGADA));
            } else {
                fila.saida(tempo, eventos, servidores, times);
            }
            count--;
            tempoGlobal = tempo;

            if(eventos.size() > capacidade){
                perdaClientes++;
            }
        }

        System.out.println("Número de servidores: " + servidores + "\n");
        System.out.println("Número de clientes perdidos: " + perdaClientes + "\n");
        System.out.println("Distribuição de probabilidade dos estados da fila:");
        for (int i = 0; i < capacidade + 1; i++) {
            double probabilidade = (double) times[i] / tempoGlobal * 100;
            System.out.println(i + ": " + times[i] + " (" + probabilidade + "%)");
        }
        System.out.println("\nTempo global da simulação: " + tempoGlobal);
    }
}
