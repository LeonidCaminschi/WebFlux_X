package md.esempla.webflux.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import md.esempla.webflux.domain.Post;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Post}, with proper type conversions.
 */
@Service
public class PostRowMapper implements BiFunction<Row, String, Post> {

    private final ColumnConverter converter;

    public PostRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Post} stored in the database.
     */
    @Override
    public Post apply(Row row, String prefix) {
        Post entity = new Post();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setTitle(converter.fromRow(row, prefix + "_title", String.class));
        entity.setContent(converter.fromRow(row, prefix + "_content", String.class));
        entity.setCreateTime(converter.fromRow(row, prefix + "_create_time", ZonedDateTime.class));
        entity.setUpdateTime(converter.fromRow(row, prefix + "_update_time", ZonedDateTime.class));
        entity.setPostStatusId(converter.fromRow(row, prefix + "_post_status_id", Long.class));
        return entity;
    }
}
