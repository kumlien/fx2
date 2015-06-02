package hoggaster.reactor;

import hoggaster.domain.Instrument;

import org.junit.Test;
import org.reactivestreams.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.processor.RingBufferProcessor;
import reactor.core.processor.RingBufferWorkProcessor;
import reactor.rx.Stream;
import reactor.rx.Streams;

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
	p.subscribe(p);
	Thread.sleep(2500);
    }
    
    @Test
    public void testGetBidAskCandlesWithStreams() {
	reactor.Environment.initializeIfEmpty();
	Processor<String,String> processor = RingBufferProcessor.create();

	Stream<String> st1 = Streams.just("Hello "); 
	Stream<String> st2 = Streams.just("World "); 
	Stream<String> st3 = Streams.wrap(processor); 

	Streams.concat(st1, st2, st3) 
	  .reduce( (prev, next) -> prev + next ) 
	  .consume(s -> System.out.printf("%s greeting = %s%n", Thread.currentThread(), s)); 

	processor.onNext("!");
	processor.onComplete();
    }
    
    
    @Test
    public void test1() {
	Stream<Instrument> coolStream = Streams.from(Instrument.values());
	coolStream.consume(i -> LOG.info("Instrument {}", i));
	
    }
}
