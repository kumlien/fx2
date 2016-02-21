package hoggaster;

import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Currency;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {


    @Override
    public CustomConversions customConversions() {
        return new CustomConversions(Arrays.asList(
                //new InstantToLongConverter(),
                new DBObjectToInstantConverter(),
                new DbObjectToCurrencyConverter(),
                new StringToGrantedAuthorityConverterConverter(), new GrantedAuthorityToStringConverterConverter(),
                new LocalDateToStringConverter(), new StringToLocalDateConverter()));
    }

    public class StringToGrantedAuthorityConverterConverter implements Converter<String, GrantedAuthority> {
        @Override
        public GrantedAuthority convert(String source) {
            return new SimpleGrantedAuthority(source);
        }
    }

    public class GrantedAuthorityToStringConverterConverter implements Converter<GrantedAuthority, String> {

        @Override
        public String convert(GrantedAuthority source) {
            return source.getAuthority();
        }
    }


    public class DbObjectToCurrencyConverter implements Converter<DBObject, Currency> {
        @Override
        public Currency convert(DBObject source) {
            return Currency.getInstance((String) source.get("currencyCode"));
        }
    }

    public class InstantToLongConverter implements Converter<Instant, Long> {
        @Override
        public Long convert(Instant instant) {
            return instant.toEpochMilli();
        }
    }

    public class DBObjectToInstantConverter implements Converter<DBObject, Instant> {
        @Override
        public Instant convert(DBObject source) {
            return Instant.ofEpochSecond((Long)source.get("seconds"), (Integer)source.get("nanos"));
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
