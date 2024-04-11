import java.util.PriorityQueue;
import java.util.Random;

class Event {
    public enum Type {
        ARRIVAL, DEPARTURE, PASSAGE
    }

    private Type type;
    private double time;

    public Event(Type type, double time) {
        this.type = type;
        this.time = time;
    }

    public Type getType() {
        return type;
    }

    public double getTime() {
        return time;
    }
}

class Scheduler {
    private PriorityQueue<Event> events = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));
    private long M;

    public Scheduler(long M) {
        this.M = M;
    }

    public void schedule(Event event) {
        events.add(event);
    }

    public Event nextEvent() {
        return events.peek();
    }

    public void removeFirst() {
        events.poll();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    // Método para contar os eventos em um determinado tempo
    public int countEvents(double time) {
        int count = 0;
        for (Event event : events) {
            if (event.getTime() == time) {
                count++;
            }
        }
        return count;
    }
}

class ExponentialRandomStream {
    private static Random random = new Random();

    public static double generate(double rate, long a, long c, long M, long seed) {
        random.setSeed(seed);
        double u = random.nextDouble();
        return -(Math.log(1 - u) / rate);
    }
}

public class QueueSimulator {
    private double arrivalRate;
    private double serviceRate;
    private int capacity;
    private long seed;
    private long a;
    private long c;
    private long M;
    private int numEvents;
    private int numEvents2;
    private Scheduler scheduler1;
    private Scheduler scheduler2;
    private double time;

    public QueueSimulator(double arrivalRate, double serviceRate, int capacity, long seed, int numEvents, int numEvents2, long a, long c, long M1, long M2) {
        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.capacity = capacity;
        this.seed = seed;
        this.numEvents = numEvents;
        this.numEvents2 = numEvents2;
        this.a = a;
        this.c = c;
        this.M = M;
        this.scheduler1 = new Scheduler(M1);
        this.scheduler2 = new Scheduler(M2);
        this.time = 0.0;
    }

    public void simulate() {
        double arrivalTime = 0;
        double serviceTime = 0;
        double currentTime = 0;

        // Schedule the first arrival event
        arrivalTime = ExponentialRandomStream.generate(arrivalRate, a, c, M, seed);
        scheduler1.schedule(new Event(Event.Type.ARRIVAL, arrivalTime));

        // Run the simulation
        while (currentTime < numEvents) {
            Event nextEvent;
            if (!scheduler2.isEmpty() && (scheduler1.isEmpty() || scheduler2.nextEvent().getTime() < scheduler1.nextEvent().getTime())) {
                nextEvent = scheduler2.nextEvent();
            } else {
                nextEvent = scheduler1.nextEvent();
            }
            time = nextEvent.getTime();
            currentTime = time;

            if (nextEvent.getType() == Event.Type.ARRIVAL) {
                if (currentTime < numEvents) {
                    if (nextEvent == scheduler1.nextEvent()) {
                        arrivalTime = currentTime + ExponentialRandomStream.generate(arrivalRate, a, c, M, seed);
                        scheduler1.schedule(new Event(Event.Type.ARRIVAL, arrivalTime));
                    } else {
                        arrivalTime = currentTime + ExponentialRandomStream.generate(arrivalRate, a, c, M, seed);
                        scheduler2.schedule(new Event(Event.Type.PASSAGE, arrivalTime));
                    }
                }
            } else if (nextEvent.getType() == Event.Type.DEPARTURE) {
                if (currentTime < numEvents) {
                    serviceTime = currentTime + ExponentialRandomStream.generate(serviceRate, a, c, M, seed);
                    scheduler1.schedule(new Event(Event.Type.DEPARTURE, serviceTime));
                }
            } else if (nextEvent.getType() == Event.Type.PASSAGE) {
                if (currentTime < numEvents2) {
                    serviceTime = currentTime + ExponentialRandomStream.generate(serviceRate, a, c, M, seed);
                    scheduler2.schedule(new Event(Event.Type.PASSAGE, serviceTime));
                }
            }

            // Remove the processed event
            if (nextEvent.getType() != Event.Type.PASSAGE) {
                scheduler1.removeFirst();
            } else {
                scheduler2.removeFirst();
            }
        }
    }

    public void printDistribution() {
        System.out.println("Time\tQueue1\tQueue2");
        for (int i = 0; i < numEvents; i++) {
            int count1 = scheduler1.countEvents(i);
            int count2 = scheduler2.countEvents(i);
            System.out.println(i + "\t" + count1 + "\t" + count2);
        }
    }

    public static void main(String[] args) {
        // Parâmetros da simulação
        double arrivalRate = 0.5;  // Taxa de chegada de clientes por unidade de tempo
        double serviceRate = 0.6;  // Taxa de atendimento de clientes por unidade de tempo
        int capacity = 10;         // Capacidade máxima da fila
        int numEvents = 100000;    // Número total de eventos a simular
        long seed = 12345;         // Semente para o gerador de números pseudoaleatórios
        long seed1 = 12346;        // Semente para o gerador de números pseudoaleatórios 1
        long seed2 = 12347;        // Semente para o gerador de números pseudoaleatórios 2
        long a = 1664525;          // Parâmetro 'a' do método congruente linear
        long c = 1013904223;       // Parâmetro 'c' do método congruente linear
        long M = (long) Math.pow(2, 32); // Parâmetro 'M' do método congruente linear

        // Criar o simulador de fila
        QueueSimulator simulator = new QueueSimulator(arrivalRate, serviceRate, capacity, seed, numEvents, numEvents, a, c, M, M);

        // Executar a simulação
        simulator.simulate();

        // Imprimir a distribuição de probabilidade dos estados da fila
        simulator.printDistribution();
    }
}
