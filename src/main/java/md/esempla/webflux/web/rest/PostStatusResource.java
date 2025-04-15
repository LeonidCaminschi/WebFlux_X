package md.esempla.webflux.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import md.esempla.webflux.domain.PostStatus;
import md.esempla.webflux.repository.PostStatusRepository;
import md.esempla.webflux.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link md.esempla.webflux.domain.PostStatus}.
 */
@RestController
@RequestMapping("/api/post-statuses")
@Transactional
public class PostStatusResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostStatusResource.class);

    private static final String ENTITY_NAME = "postStatus";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PostStatusRepository postStatusRepository;

    public PostStatusResource(PostStatusRepository postStatusRepository) {
        this.postStatusRepository = postStatusRepository;
    }

    /**
     * {@code POST  /post-statuses} : Create a new postStatus.
     *
     * @param postStatus the postStatus to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new postStatus, or with status {@code 400 (Bad Request)} if the postStatus has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<PostStatus>> createPostStatus(@Valid @RequestBody PostStatus postStatus) throws URISyntaxException {
        LOG.debug("REST request to save PostStatus : {}", postStatus);
        if (postStatus.getId() != null) {
            throw new BadRequestAlertException("A new postStatus cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return postStatusRepository
            .save(postStatus)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/post-statuses/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /post-statuses/:id} : Updates an existing postStatus.
     *
     * @param id the id of the postStatus to save.
     * @param postStatus the postStatus to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated postStatus,
     * or with status {@code 400 (Bad Request)} if the postStatus is not valid,
     * or with status {@code 500 (Internal Server Error)} if the postStatus couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<PostStatus>> updatePostStatus(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody PostStatus postStatus
    ) throws URISyntaxException {
        LOG.debug("REST request to update PostStatus : {}, {}", id, postStatus);
        if (postStatus.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, postStatus.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return postStatusRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return postStatusRepository
                    .save(postStatus)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code GET  /post-statuses} : get all the postStatuses.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of postStatuses in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<PostStatus>> getAllPostStatuses(@RequestParam(name = "filter", required = false) String filter) {
        if ("post-is-null".equals(filter)) {
            LOG.debug("REST request to get all PostStatuss where post is null");
            return postStatusRepository.findAllWherePostIsNull().collectList();
        }
        LOG.debug("REST request to get all PostStatuses");
        return postStatusRepository.findAll().collectList();
    }

    /**
     * {@code GET  /post-statuses} : get all the postStatuses as a stream.
     * @return the {@link Flux} of postStatuses.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<PostStatus> getAllPostStatusesAsStream() {
        LOG.debug("REST request to get all PostStatuses as a stream");
        return postStatusRepository.findAll();
    }

    /**
     * {@code GET  /post-statuses/:id} : get the "id" postStatus.
     *
     * @param id the id of the postStatus to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the postStatus, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PostStatus>> getPostStatus(@PathVariable("id") Long id) {
        LOG.debug("REST request to get PostStatus : {}", id);
        Mono<PostStatus> postStatus = postStatusRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(postStatus);
    }

    /**
     * {@code DELETE  /post-statuses/:id} : delete the "id" postStatus.
     *
     * @param id the id of the postStatus to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePostStatus(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete PostStatus : {}", id);
        return postStatusRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
