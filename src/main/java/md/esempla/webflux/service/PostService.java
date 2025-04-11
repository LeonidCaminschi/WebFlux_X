package md.esempla.webflux.service;

import md.esempla.webflux.domain.Post;
import md.esempla.webflux.domain.criteria.PostCriteria;
import md.esempla.webflux.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link md.esempla.webflux.domain.Post}.
 */
@Service
@Transactional
public class PostService {

    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Save a post.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    public Mono<Post> save(Post post) {
        LOG.debug("Request to save Post : {}", post);
        return postRepository.save(post);
    }

    /**
     * Update a post.
     *
     * @param post the entity to save.
     * @return the persisted entity.
     */
    public Mono<Post> update(Post post) {
        LOG.debug("Request to update Post : {}", post);
        return postRepository.save(post);
    }

    /**
     * Partially update a post.
     *
     * @param post the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Post> partialUpdate(Post post) {
        LOG.debug("Request to partially update Post : {}", post);

        return postRepository
            .findById(post.getId())
            .map(existingPost -> {
                if (post.getTitle() != null) {
                    existingPost.setTitle(post.getTitle());
                }
                if (post.getContent() != null) {
                    existingPost.setContent(post.getContent());
                }
                if (post.getCreateTime() != null) {
                    existingPost.setCreateTime(post.getCreateTime());
                }
                if (post.getUpdateTime() != null) {
                    existingPost.setUpdateTime(post.getUpdateTime());
                }

                return existingPost;
            })
            .flatMap(postRepository::save);
    }

    /**
     * Find posts by Criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Post> findByCriteria(PostCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Posts by Criteria");
        return postRepository.findByCriteria(criteria, pageable);
    }

    /**
     * Find the count of posts by criteria.
     * @param criteria filtering criteria
     * @return the count of posts
     */
    public Mono<Long> countByCriteria(PostCriteria criteria) {
        LOG.debug("Request to get the count of all Posts by Criteria");
        return postRepository.countByCriteria(criteria);
    }

    /**
     * Returns the number of posts available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return postRepository.count();
    }

    /**
     * Get one post by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Post> findOne(Long id) {
        LOG.debug("Request to get Post : {}", id);
        return postRepository.findById(id);
    }

    /**
     * Delete the post by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Post : {}", id);
        return postRepository.deleteById(id);
    }
}
