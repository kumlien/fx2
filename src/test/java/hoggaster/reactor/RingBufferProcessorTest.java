package hoggaster.reactor;

import hoggaster.domain.CurrencyPair;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Environment;
import reactor.core.processor.RingBufferProcessor;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.fn.tuple.Tuple;
import reactor.fn.tuple.Tuple2;
import reactor.rx.Stream;
import reactor.rx.Streams;
import reactor.rx.broadcast.Broadcaster;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class RingBufferProcessorTest {

    private static final Logger LOG = LoggerFactory.getLogger(RingBufferProcessorTest.class);


    @Test
    public void test() throws InterruptedException {
        RingBufferWorkProcessor<Integer> p = RingBufferWorkProcessor.create("test", 32);
        Stream<Integer> s = Streams.wrap(p);
        s.consume(i -> System.out.println(Thread.currentThread() + " data=" + i));
        s.consume(i -> System.out.println(Thread.currentThread() + " data=" + i));
        p.onNext(1);
        p.onNext(2);
        //p.subscribe(p);
        Thread.sleep(2500);
    }

    @Test
    public void testGetBidAskCandlesWithStreams() {
        reactor.Environment.initializeIfEmpty();
        Processor<String, String> processor = RingBufferProcessor.create();

        Stream<String> st1 = Streams.just("Hello ");
        Stream<String> st2 = Streams.just("World ");
        Stream<String> st3 = Streams.wrap(processor);

        Streams.concat(st1, st2, st3)
                .reduce((prev, next) -> prev + next)
                .consume(s -> System.out.printf("%s greeting = %s%n", Thread.currentThread(), s));

        processor.onNext("!");
        processor.onComplete();
    }


    @Test
    public void test1() {
        Stream<CurrencyPair> coolStream = Streams.from(CurrencyPair.values());
        coolStream.consume(i -> LOG.info("CurrencyPair {}", i));

    }

    @Test
    public void testHotStream() throws InterruptedException {

        Broadcaster<Tuple2<Long, String>> sink = Broadcaster.create(Environment.initializeIfEmpty());
        final Map<Long, String> runningImports = new HashMap<>();

        sink
                .capacity(2)
                .adaptiveConsume(
                        tuple -> {
                            System.out.printf("Starting import with tuple %s: %s%n", tuple.t1, tuple.t2);
                            runningImports.put(tuple.t1, tuple.t2);
                            System.out.println("Running imports is now " + runningImports.size());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("Removing " + tuple.t1);
                                    runningImports.remove(tuple.t1);
                                }
                            }).start();


                        },
                        (Stream<Long> stream) -> {
                            return new org.reactivestreams.Publisher<Long>() {
                                @Override
                                public void subscribe(Subscriber<? super Long> subscriber) {
                                    stream.consume(batchSize -> {
                                        System.out.println("Batchsize is " + batchSize + " and running imports is " + runningImports.size());
                                        while (runningImports.size() >= 2) {
                                            System.out.println("Wait 500...");
                                            try {
                                                Thread.sleep(500);
                                            } catch (Exception e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                        }

                                        subscriber.onNext(batchSize);

                                    });
                                }
                            };
                        });

        Stream<Long> range = Streams.range(1, 10);
        range.consume(n -> sink.onNext(Tuple.of(n, "Hello number " + n)));

        Thread.sleep(1000);
        System.out.println("Running before  wait loop: " + runningImports.size());
        while (runningImports.size() > 0) {
            runningImports.keySet().stream().forEach(id -> System.out.println(id + ", "));
            Thread.sleep(5000);
        }
    }
}
