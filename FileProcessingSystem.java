import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Task class (Worker Thread)
class FileTask implements Callable<String> {

    private File file;
    private AtomicInteger counter;

    public FileTask(File file, AtomicInteger counter) {
        this.file = file;
        this.counter = counter;
    }

    @Override
    public String call() {

        long start = System.currentTimeMillis();

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));

            int lines = 0;
            int words = 0;

            String line;

            while ((line = br.readLine()) != null) {

                lines++;

                words += line.split("\\s+").length;
            }

            br.close();

            counter.incrementAndGet();

            long end = System.currentTimeMillis();

            return file.getName()
                    + " | Lines: " + lines
                    + " | Words: " + words
                    + " | Thread: "
                    + Thread.currentThread().getName()
                    + " | Time: " + (end - start) + "ms";

        } catch (Exception e) {

            return file.getName()
                    + " | ERROR";
        }
    }
}

// Manager class
class FileManager {

    ExecutorService pool;

    AtomicInteger processed = new AtomicInteger(0);

    List<Future<String>> results = new ArrayList<>();

    public FileManager(int threads) {

        pool = Executors.newFixedThreadPool(threads);
    }

    public void processFolder(String path)
            throws Exception {

        File folder = new File(path);

        File[] files = folder.listFiles();

        if (files == null) {

            System.out.println("Folder empty");
            return;
        }

        for (File file : files) {

            if (file.isFile()) {

                FileTask task = new FileTask(file, processed);

                Future<String> future = pool.submit(task);

                results.add(future);
            }
        }

        showResults();
    }

    public void showResults()
            throws Exception {

        for (Future<String> f : results) {

            System.out.println(f.get());
        }

        System.out.println(
                "\nTotal processed: "
                        + processed.get());
    }

    public void shutdown() {

        pool.shutdown();
    }
}

// Producer Consumer
class ProducerConsumer {

    BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    class Producer extends Thread {

        public void run() {

            try {

                for (int i = 1; i <= 5; i++) {

                    String file = "File" + i;

                    queue.put(file);

                    System.out.println(
                            "Produced: " + file);

                    Thread.sleep(500);
                }

                queue.put("END");

            } catch (Exception e) {
            }
        }
    }

    class Consumer extends Thread {

        public void run() {

            try {

                while (true) {

                    String file = queue.take();

                    if (file.equals("END"))
                        break;

                    System.out.println(
                            "Consumed: " + file);

                    Thread.sleep(800);
                }

            } catch (Exception e) {
            }
        }
    }

    public void startSystem()
            throws Exception {

        Producer p = new Producer();

        Consumer c1 = new Consumer();

        Consumer c2 = new Consumer();

        p.start();
        c1.start();
        c2.start();

        p.join();
        c1.join();
        c2.join();
    }
}

// Main class
public class FileProcessingSystem {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args)
            throws Exception {

        while (true) {

            System.out.println(
                    "\n=== FILE SYSTEM ===");

            System.out.println(
                    "1 Process Folder");

            System.out.println(
                    "2 Producer Consumer");

            System.out.println(
                    "3 Thread Info");

            System.out.println(
                    "4 Exit");

            int choice = sc.nextInt();

            sc.nextLine();

            switch (choice) {

                case 1:

                    System.out.print(
                            "Enter folder path: ");

                    String path = sc.nextLine();

                    System.out.print(
                            "Threads: ");

                    int t = sc.nextInt();

                    FileManager fm = new FileManager(t);

                    fm.processFolder(path);

                    fm.shutdown();

                    break;

                case 2:

                    ProducerConsumer pc = new ProducerConsumer();

                    pc.startSystem();

                    break;

                case 3:

                    Set<Thread> threads = Thread.getAllStackTraces()
                            .keySet();

                    for (Thread th : threads) {

                        System.out.println(
                                th.getName()
                                        + " | "
                                        + th.getState());
                    }

                    break;

                case 4:

                    System.exit(0);
            }
        }
    }
}
