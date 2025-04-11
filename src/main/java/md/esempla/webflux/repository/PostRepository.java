package md.esempla.webflux.repository;

import md.esempla.webflux.domain.Post;
import md.esempla.webflux.domain.criteria.PostCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, Long>, PostRepositoryInternal {
    Flux<Post> findAllBy(Pageable pageable);

    @Query("SELECT * FROM post entity WHERE entity.post_status_id = :id")
    Flux<Post> findByPostStatus(Long id);

    @Query("SELECT * FROM post entity WHERE entity.post_status_id IS NULL")
    Flux<Post> findAllWherePostStatusIsNull();

    @Override
    <S extends Post> Mono<S> save(S entity);

    @Override
    Flux<Post> findAll();

    @Override
    Mono<Post> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PostRepositoryInternal {
    <S extends Post> Mono<S> save(S entity);

    Flux<Post> findAllBy(Pageable pageable);

    Flux<Post> findAll();

    Mono<Post> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Post> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Post> findByCriteria(PostCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(PostCriteria criteria);
}
