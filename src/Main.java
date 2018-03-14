import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String... args) {
        test();
    }

    private static void safeRandomSleep(int i) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(i));
        } catch (InterruptedException e) {

        }
    }

    private static void test() {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10_000_000; i++) {

           CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
                safeRandomSleep(10);
                return "S";
            }, executor);

            CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
                safeRandomSleep(10);
                throw new RuntimeException("123");
            }, executor);

            ArrayList<CompletableFuture<?>> ff = new ArrayList<>();
            ff.add(f1);
            ff.add(f2);

            for (int j = 0; j < 3; j++) {
                ff.add(f1.thenCombineAsync(f2, (s1, s2) -> s1 + s2, executor));
                ff.add(f1.thenApplyAsync((s) -> s, executor));
            }

            Collections.shuffle(ff, ThreadLocalRandom.current());
            CompletableFuture<Void> all = CompletableFuture.allOf(ff.toArray(new CompletableFuture[0]));
            all.whenComplete((aVoid, throwable) -> System.out.print("."));

            safeRandomSleep(10);

            Collections.shuffle(ff, ThreadLocalRandom.current());
            CompletableFuture<Void> all2 = CompletableFuture.allOf(ff.toArray(new CompletableFuture[0]));
            all2.whenComplete((aVoid, throwable) -> System.out.print("."));

            try {
                CompletableFuture<Void> a = all.thenApplyAsync(s -> s, executor);
                CompletableFuture<Void> b = all2.thenApplyAsync(s -> s, executor);
                a.join();
                b.join();
            } catch (Exception e) {
            }
            System.out.println(i);
        }

    }

}
