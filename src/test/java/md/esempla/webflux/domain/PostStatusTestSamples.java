package md.esempla.webflux.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PostStatusTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static PostStatus getPostStatusSample1() {
        return new PostStatus().id(1L).status("status1");
    }

    public static PostStatus getPostStatusSample2() {
        return new PostStatus().id(2L).status("status2");
    }

    public static PostStatus getPostStatusRandomSampleGenerator() {
        return new PostStatus().id(longCount.incrementAndGet()).status(UUID.randomUUID().toString());
    }
}
