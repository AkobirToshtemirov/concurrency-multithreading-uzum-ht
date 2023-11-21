import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class FileAverageCalculator {
    private static double totalSum = 0;
    private static int totalNumbers = 0;

    public static void main(String[] args) {
        if (args.length < 4 || !args[0].equals("--files") || !args[2].equals("--par")) {
            System.out.println("Usage: java FileAverageCalculator --files \"file1.txt,file2.txt,...\" --par <parallelism level>");
            return;
        }

        List<String> files = Arrays.asList(args[1].split(","));
        int parallelismLevel = Integer.parseInt(args[3]);

        if (parallelismLevel <= 0) {
            System.out.println("Parallelism level should be a positive integer.");
            return;
        }

        calculateOverallAverage(files, parallelismLevel);
    }

    private static void calculateOverallAverage(List<String> files, int parallelismLevel) {
        ExecutorService executor = Executors.newFixedThreadPool(parallelismLevel);

        for (String filePath : files) {
            executor.submit(new FileProcessor(filePath));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double overallAverage = totalSum / totalNumbers;
        System.out.println("Overall Average: " + overallAverage);
    }

    static class FileProcessor implements Runnable {
        private String filePath;

        FileProcessor(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                List<String> lines = Files.readAllLines(Paths.get(filePath));
                int sum = lines.stream().mapToInt(Integer::parseInt).sum();

                synchronized (FileAverageCalculator.class) {
                    totalSum += sum;
                    totalNumbers += lines.size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}