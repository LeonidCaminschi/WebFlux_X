package md.esempla.webflux.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import md.esempla.webflux.domain.Post;
import md.esempla.webflux.domain.criteria.PostCriteria;
import md.esempla.webflux.repository.rowmapper.ColumnConverter;
import md.esempla.webflux.repository.rowmapper.PostRowMapper;
import md.esempla.webflux.repository.rowmapper.PostStatusRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Post entity.
 */
@SuppressWarnings("unused")
class PostRepositoryInternalImpl extends SimpleR2dbcRepository<Post, Long> implements PostRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final PostStatusRowMapper poststatusMapper;
    private final PostRowMapper postMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("post", EntityManager.ENTITY_ALIAS);
    private static final Table postStatusTable = Table.aliased("post_status", "postStatus");

    public PostRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        PostStatusRowMapper poststatusMapper,
        PostRowMapper postMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Post.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.poststatusMapper = poststatusMapper;
        this.postMapper = postMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Post> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Post> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = PostSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(PostStatusSqlHelper.getColumns(postStatusTable, "postStatus"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(postStatusTable)
            .on(Column.create("post_status_id", entityTable))
            .equals(Column.create("id", postStatusTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Post.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Post> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Post> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Post process(Row row, RowMetadata metadata) {
        Post entity = postMapper.apply(row, "e");
        entity.setPostStatus(poststatusMapper.apply(row, "postStatus"));
        return entity;
    }

    @Override
    public <S extends Post> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Post> findByCriteria(PostCriteria postCriteria, Pageable page) {
        return createQuery(page, buildConditions(postCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(PostCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(PostCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getTitle() != null) {
                builder.buildFilterConditionForField(criteria.getTitle(), entityTable.column("title"));
            }
            if (criteria.getContent() != null) {
                builder.buildFilterConditionForField(criteria.getContent(), entityTable.column("content"));
            }
            if (criteria.getCreateTime() != null) {
                builder.buildFilterConditionForField(criteria.getCreateTime(), entityTable.column("create_time"));
            }
            if (criteria.getUpdateTime() != null) {
                builder.buildFilterConditionForField(criteria.getUpdateTime(), entityTable.column("update_time"));
            }
            if (criteria.getPostStatusId() != null) {
                builder.buildFilterConditionForField(criteria.getPostStatusId(), postStatusTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
