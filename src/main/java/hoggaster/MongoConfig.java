package hoggaster;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
    
    
    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(
                new InstantToLongConverter(), new LongToInstantConverter(),
                new LocalDateToStringConverter(), new StringToLocalDateConverter()));
    }

    
    public class InstantToLongConverter implements Converter<Instant, Long> {
	    @Override
	    public Long convert(Instant instant) {
	        return instant.toEpochMilli();
	    }
	}

	public class LongToInstantConverter implements Converter<Long, Instant> {
	    @Override
	    public Instant convert(Long source) {
	        return Instant.ofEpochMilli(source);
	    }
	}

	public class LocalDateToStringConverter implements Converter<LocalDate, String> {
	    @Override
	    public String convert(LocalDate localDate) {
	        return localDate.toString();
	    }
	}

	public class StringToLocalDateConverter implements Converter<String, LocalDate> {
	    @Override
	    public LocalDate convert(String source) {
	        return LocalDate.parse(source);
	    }
	}

	@Override
	protected String getDatabaseName() {
	    return "fx2";
	}

	@Override
	public Mongo mongo() throws Exception {
	    return new MongoClient();
	}
}
