package md.esempla.webflux.service;

import md.esempla.webflux.domain.Comment;
import md.esempla.webflux.domain.criteria.CommentCriteria;
import md.esempla.webflux.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link md.esempla.webflux.domain.Comment}.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Save a comment.
     *
     * @param comment the entity to save.
     * @return the persisted entity.
     */
    public Mono<Comment> save(Comment comment) {
        LOG.debug("Request to save Comment : {}", comment);
        return commentRepository.save(comment);
    }

    /**
     * Update a comment.
     *
     * @param comment the entity to save.
     * @return the persisted entity.
     */
    public Mono<Comment> update(Comment comment) {
        LOG.debug("Request to update Comment : {}", comment);
        return commentRepository.save(comment);
    }

    /**
     * Partially update a comment.
     *
     * @param comment the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Comment> partialUpdate(Comment comment) {
        LOG.debug("Request to partially update Comment : {}", comment);

        return commentRepository
            .findById(comment.getId())
            .map(existingComment -> {
                if (comment.getContent() != null) {
                    existingComment.setContent(comment.getContent());
                }
                if (comment.getCreateTime() != null) {
                    existingComment.setCreateTime(comment.getCreateTime());
                }

                return existingComment;
            })
            .flatMap(commentRepository::save);
    }

    /**
     * Find comments by Criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Comment> findByCriteria(CommentCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Comments by Criteria");
        return commentRepository.findByCriteria(criteria, pageable);
    }

    /**
     * Find the count of comments by criteria.
     * @param criteria filtering criteria
     * @return the count of comments
     */
    public Mono<Long> countByCriteria(CommentCriteria criteria) {
        LOG.debug("Request to get the count of all Comments by Criteria");
        return commentRepository.countByCriteria(criteria);
    }

    /**
     * Returns the number of comments available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return commentRepository.count();
    }

    /**
     * Get one comment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Comment> findOne(Long id) {
        LOG.debug("Request to get Comment : {}", id);
        return commentRepository.findById(id);
    }

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        LOG.debug("Request to delete Comment : {}", id);
        return commentRepository.deleteById(id);
    }
}
