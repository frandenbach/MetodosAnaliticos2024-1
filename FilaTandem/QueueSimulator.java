import java.util.ArrayList;
import java.util.List;
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
    public Scheduler(long M) {
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

class Queue {
    private PriorityQueue<Event> events;
    private int capacity;
    private int numServers;

    public Queue(long seed2, int numServers) {
        this.capacity = (int) seed2;
        this.numServers = numServers;
        this.events = new PriorityQueue<>((e1, e2) -> Double.compare(e1.getTime(), e2.getTime()));
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

    public boolean isFull() {
        return events.size() >= capacity;
    }

    public boolean hasServersAvailable() {
        return events.size() < numServers;
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }

    public List<Event> getEvents() {
        return new ArrayList<>(events);
    }
}

public class QueueSimulator {
    private double arrivalInterval;
    private double serviceInterval2;
    private int numEvents;
    private long seed;
    private long a;
    private long c;
    private long M;
    private Queue queue1;
    private Queue queue2;
    private double time;
    private int lostCustomers1;
    private int lostCustomers2;

    public QueueSimulator(double arrivalInterval, double serviceInterval1, double serviceInterval2,
                      int capacity1, int capacity2, long seed, int numEvents, long a, long c, long M) {
        this.arrivalInterval = arrivalInterval;
        this.serviceInterval2 = serviceInterval2;
        this.seed = seed;
        this.numEvents = numEvents;
        this.a = a;
        this.c = c;
        this.M = M;
        this.queue1 = new Queue(seed, capacity1);
        this.queue2 = new Queue(seed, capacity2);
        this.time = 0.0;
        this.lostCustomers1 = 0;
        this.lostCustomers2 = 0;
    }

    public void simulate() {
        while (numEvents > 0 && (!queue1.isEmpty() || !queue2.isEmpty())) {
            Event nextEvent;
            if (!queue2.isEmpty() && (queue1.isEmpty() || queue2.nextEvent().getTime() < queue1.nextEvent().getTime())) {
                nextEvent = queue2.nextEvent();
            } else {
                nextEvent = queue1.nextEvent();
            }
            if (nextEvent == null) break;
            time = nextEvent.getTime();
            if (nextEvent.getType() == Event.Type.ARRIVAL) {
                if (nextEvent == queue1.nextEvent()) {
                    if (queue1.hasServersAvailable()) {
                        double nextArrival = time + ExponentialRandomStream.generate(arrivalInterval, a, c, M, seed);
                        queue1.schedule(new Event(Event.Type.ARRIVAL, nextArrival));
                    } else if (queue2.hasServersAvailable()) {
                        double serviceTime = time + ExponentialRandomStream.generate(serviceInterval2, a, c, M, seed);
                        queue2.schedule(new Event(Event.Type.DEPARTURE, serviceTime));
                    } else {
                        lostCustomers1++;
                    }
                } else {
                    if (!queue2.isFull()) {
                        double serviceTime = time + ExponentialRandomStream.generate(serviceInterval2, a, c, M, seed);
                        queue2.schedule(new Event(Event.Type.DEPARTURE, serviceTime));
                    } else {
                        lostCustomers2++;
                    }
                }
            } else if (nextEvent.getType() == Event.Type.DEPARTURE) {
                if (nextEvent == queue1.nextEvent()) {
                    queue1.removeFirst();
                } else {
                    queue2.removeFirst();
                }
            }
            numEvents--;
        }
    }

    public void printDistribution() {
        System.out.println("Time\tQueue1\tQueue2");
        int totalTime1 = 0;
        int totalTime2 = 0;
        int totalCustomers1 = 0;
        int totalCustomers2 = 0;
        int totalArrivals1 = 0;
        int totalArrivals2 = 0;
        int totalDepartures1 = 0;
        int totalDepartures2 = 0;
        int currentTime = 0;

        while (currentTime <= time) {
            int count1 = countEvents(queue1, currentTime);
            int count2 = countEvents(queue2, currentTime);
            System.out.println(currentTime + "\t" + count1 + "\t" + count2);

            totalTime1 += count1;
            totalTime2 += count2;
            totalCustomers1 += count1;
            totalCustomers2 += count2;

            if (currentTime == 0) {
                totalArrivals1 = count1;
                totalArrivals2 = count2;
            }
            currentTime++;
        }

        totalDepartures2 = countDepartures(queue2);
        totalDepartures1 = countDepartures(queue1);

        double avgCustomers1 = totalArrivals1 != 0 ? (double) totalCustomers1 / totalArrivals1 : 0;
        double avgCustomers2 = totalArrivals2 != 0 ? (double) totalCustomers2 / totalArrivals2 : 0;
        double avgTime1 = totalDepartures1 != 0 ? (double) totalTime1 / totalDepartures1 : 0;
        double avgTime2 = totalDepartures2 != 0 ? (double) totalTime2 / totalDepartures2 : 0;

        System.out.println("Average number of customers in Queue 1: " + avgCustomers1);
        System.out.println("Average number of customers in Queue 2: " + avgCustomers2);
        System.out.println("Average time in Queue 1: " + avgTime1);
        System.out.println("Average time in Queue 2: " + avgTime2);
        System.out.println("Lost customers in Queue 1: " + lostCustomers1);
        System.out.println("Lost customers in Queue 2: " + lostCustomers2);
        System.out.println("Simulation time: " + time);
    }

    private int countEvents(Queue queue, int time) {
        int count = 0;
        for (Event event : queue.getEvents()) {
            if (event.getTime() == time) {
                count++;
            }
        }
        return count;
    }

    private int countDepartures(Queue queue) {
        int count = 0;
        for (Event event : queue.getEvents()) {
            if (event.getType() == Event.Type.DEPARTURE) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        // Parâmetros da fila 1
        double arrivalInterval1 = 4 - 1;
        double serviceInterval1 = 4 - 3;
        int capacity1 = 3;

        // Parâmetros da fila 2
        double serviceInterval2 = 3 - 2;
        int capacity2 = 5;

        // Parâmetros comuns para ambas as filas
        int numEvents = 100000;
        long seed = 12345;
        long a = 1664525;
        long c = 1013904223;
        long M = (long) Math.pow(2, 32);

        QueueSimulator simulator = new QueueSimulator(arrivalInterval1, serviceInterval1, serviceInterval2,
                                                      capacity1, capacity2, seed, numEvents, a, c, M);

        simulator.simulate();
        simulator.printDistribution();
    }
}
