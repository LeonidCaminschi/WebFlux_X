package md.esempla.webflux.repository;

import md.esempla.webflux.domain.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the PostStatus entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostStatusRepository extends ReactiveCrudRepository<PostStatus, Long>, PostStatusRepositoryInternal {
    @Query("SELECT * FROM post_status entity WHERE entity.id not in (select post_id from post)")
    Flux<PostStatus> findAllWherePostIsNull();

    @Override
    <S extends PostStatus> Mono<S> save(S entity);

    @Override
    Flux<PostStatus> findAll();

    @Override
    Mono<PostStatus> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PostStatusRepositoryInternal {
    <S extends PostStatus> Mono<S> save(S entity);

    Flux<PostStatus> findAllBy(Pageable pageable);

    Flux<PostStatus> findAll();

    Mono<PostStatus> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<PostStatus> findAllBy(Pageable pageable, Criteria criteria);
}
