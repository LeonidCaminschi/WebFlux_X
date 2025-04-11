package md.esempla.webflux.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import md.esempla.webflux.domain.PostStatus;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link PostStatus}, with proper type conversions.
 */
@Service
public class PostStatusRowMapper implements BiFunction<Row, String, PostStatus> {

    private final ColumnConverter converter;

    public PostStatusRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link PostStatus} stored in the database.
     */
    @Override
    public PostStatus apply(Row row, String prefix) {
        PostStatus entity = new PostStatus();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", String.class));
        return entity;
    }
}
