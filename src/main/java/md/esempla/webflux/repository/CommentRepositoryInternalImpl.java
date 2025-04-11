package md.esempla.webflux.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import md.esempla.webflux.domain.Comment;
import md.esempla.webflux.domain.criteria.CommentCriteria;
import md.esempla.webflux.repository.rowmapper.ColumnConverter;
import md.esempla.webflux.repository.rowmapper.CommentRowMapper;
import md.esempla.webflux.repository.rowmapper.PostRowMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.service.ConditionBuilder;

/**
 * Spring Data R2DBC custom repository implementation for the Comment entity.
 */
@SuppressWarnings("unused")
class CommentRepositoryInternalImpl extends SimpleR2dbcRepository<Comment, Long> implements CommentRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final PostRowMapper postMapper;
    private final CommentRowMapper commentMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("comment", EntityManager.ENTITY_ALIAS);
    private static final Table postTable = Table.aliased("post", "post");

    public CommentRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        PostRowMapper postMapper,
        CommentRowMapper commentMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Comment.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.postMapper = postMapper;
        this.commentMapper = commentMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Comment> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Comment> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = CommentSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(PostSqlHelper.getColumns(postTable, "post"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(postTable)
            .on(Column.create("post_id", entityTable))
            .equals(Column.create("id", postTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Comment.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Comment> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Comment> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Comment process(Row row, RowMetadata metadata) {
        Comment entity = commentMapper.apply(row, "e");
        entity.setPost(postMapper.apply(row, "post"));
        return entity;
    }

    @Override
    public <S extends Comment> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Comment> findByCriteria(CommentCriteria commentCriteria, Pageable page) {
        return createQuery(page, buildConditions(commentCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(CommentCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(CommentCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getContent() != null) {
                builder.buildFilterConditionForField(criteria.getContent(), entityTable.column("content"));
            }
            if (criteria.getCreateTime() != null) {
                builder.buildFilterConditionForField(criteria.getCreateTime(), entityTable.column("create_time"));
            }
            if (criteria.getPostId() != null) {
                builder.buildFilterConditionForField(criteria.getPostId(), postTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
