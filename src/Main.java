import java.util.ArrayList;
import java.util.concurrent.*;

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

            CompletableFuture[] ff1 = new CompletableFuture[8];
            ff1[0] = ff.get(4);
            ff1[1] = ff.get(7);
            ff1[2] = ff.get(6);
            ff1[3] = ff.get(3);
            ff1[4] = ff.get(2);
            ff1[5] = ff.get(1);
            ff1[6] = ff.get(0);
            ff1[7] = ff.get(5);

            CompletableFuture<Void> all = CompletableFuture.allOf(ff1);
            all.whenComplete((aVoid, throwable) -> System.out.print("."));

            safeRandomSleep(10);

            CompletableFuture[] ff2 = new CompletableFuture[8];
            ff2[0] = ff.get(6);
            ff2[1] = ff.get(0);
            ff2[2] = ff.get(5);
            ff2[3] = ff.get(7);
            ff2[4] = ff.get(2);
            ff2[5] = ff.get(1);
            ff2[6] = ff.get(3);
            ff2[7] = ff.get(4);

            CompletableFuture<Void> all2 = CompletableFuture.allOf(ff2);
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
