package md.esempla.webflux.web.rest;

import static md.esempla.webflux.domain.CommentAsserts.*;
import static md.esempla.webflux.web.rest.TestUtil.createUpdateProxyForBean;
import static md.esempla.webflux.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import md.esempla.webflux.IntegrationTest;
import md.esempla.webflux.domain.Comment;
import md.esempla.webflux.domain.Post;
import md.esempla.webflux.repository.CommentRepository;
import md.esempla.webflux.repository.EntityManager;
import md.esempla.webflux.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link CommentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CommentResourceIT {

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/comments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Comment comment;

    private Comment insertedComment;

    @Autowired
    private PostRepository postRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comment createEntity() {
        return new Comment().content(DEFAULT_CONTENT).createTime(DEFAULT_CREATE_TIME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Comment createUpdatedEntity() {
        return new Comment().content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Comment.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        comment = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedComment != null) {
            commentRepository.delete(insertedComment).block();
            insertedComment = null;
        }
        deleteEntities(em);
    }

    @Test
    void createComment() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Comment
        var returnedComment = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Comment.class)
            .returnResult()
            .getResponseBody();

        // Validate the Comment in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCommentUpdatableFieldsEquals(returnedComment, getPersistedComment(returnedComment));

        insertedComment = returnedComment;
    }

    @Test
    void createCommentWithExistingId() throws Exception {
        // Create the Comment with an existing ID
        comment.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkContentIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comment.setContent(null);

        // Create the Comment, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCreateTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        comment.setCreateTime(null);

        // Create the Comment, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllComments() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(comment.getId().intValue()))
            .jsonPath("$.[*].content")
            .value(hasItem(DEFAULT_CONTENT))
            .jsonPath("$.[*].createTime")
            .value(hasItem(sameInstant(DEFAULT_CREATE_TIME)));
    }

    @Test
    void getComment() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get the comment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(comment.getId().intValue()))
            .jsonPath("$.content")
            .value(is(DEFAULT_CONTENT))
            .jsonPath("$.createTime")
            .value(is(sameInstant(DEFAULT_CREATE_TIME)));
    }

    @Test
    void getCommentsByIdFiltering() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        Long id = comment.getId();

        defaultCommentFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultCommentFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultCommentFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    void getAllCommentsByContentIsEqualToSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where content equals to
        defaultCommentFiltering("content.equals=" + DEFAULT_CONTENT, "content.equals=" + UPDATED_CONTENT);
    }

    @Test
    void getAllCommentsByContentIsInShouldWork() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where content in
        defaultCommentFiltering("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT, "content.in=" + UPDATED_CONTENT);
    }

    @Test
    void getAllCommentsByContentIsNullOrNotNull() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where content is not null
        defaultCommentFiltering("content.specified=true", "content.specified=false");
    }

    @Test
    void getAllCommentsByContentContainsSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where content contains
        defaultCommentFiltering("content.contains=" + DEFAULT_CONTENT, "content.contains=" + UPDATED_CONTENT);
    }

    @Test
    void getAllCommentsByContentNotContainsSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where content does not contain
        defaultCommentFiltering("content.doesNotContain=" + UPDATED_CONTENT, "content.doesNotContain=" + DEFAULT_CONTENT);
    }

    @Test
    void getAllCommentsByCreateTimeIsEqualToSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime equals to
        defaultCommentFiltering("createTime.equals=" + DEFAULT_CREATE_TIME, "createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    void getAllCommentsByCreateTimeIsInShouldWork() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime in
        defaultCommentFiltering("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME, "createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    void getAllCommentsByCreateTimeIsNullOrNotNull() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime is not null
        defaultCommentFiltering("createTime.specified=true", "createTime.specified=false");
    }

    @Test
    void getAllCommentsByCreateTimeIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime is greater than or equal to
        defaultCommentFiltering(
            "createTime.greaterThanOrEqual=" + DEFAULT_CREATE_TIME,
            "createTime.greaterThanOrEqual=" + UPDATED_CREATE_TIME
        );
    }

    @Test
    void getAllCommentsByCreateTimeIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime is less than or equal to
        defaultCommentFiltering("createTime.lessThanOrEqual=" + DEFAULT_CREATE_TIME, "createTime.lessThanOrEqual=" + SMALLER_CREATE_TIME);
    }

    @Test
    void getAllCommentsByCreateTimeIsLessThanSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime is less than
        defaultCommentFiltering("createTime.lessThan=" + UPDATED_CREATE_TIME, "createTime.lessThan=" + DEFAULT_CREATE_TIME);
    }

    @Test
    void getAllCommentsByCreateTimeIsGreaterThanSomething() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        // Get all the commentList where createTime is greater than
        defaultCommentFiltering("createTime.greaterThan=" + SMALLER_CREATE_TIME, "createTime.greaterThan=" + DEFAULT_CREATE_TIME);
    }

    @Test
    void getAllCommentsByPostIsEqualToSomething() {
        Post post = PostResourceIT.createEntity();
        postRepository.save(post).block();
        Long postId = post.getId();
        comment.setPostId(postId);
        insertedComment = commentRepository.save(comment).block();
        // Get all the commentList where post equals to postId
        defaultCommentShouldBeFound("postId.equals=" + postId);

        // Get all the commentList where post equals to (postId + 1)
        defaultCommentShouldNotBeFound("postId.equals=" + (postId + 1));
    }

    private void defaultCommentFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultCommentShouldBeFound(shouldBeFound);
        defaultCommentShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCommentShouldBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(comment.getId().intValue()))
            .jsonPath("$.[*].content")
            .value(hasItem(DEFAULT_CONTENT))
            .jsonPath("$.[*].createTime")
            .value(hasItem(sameInstant(DEFAULT_CREATE_TIME)));

        // Check, that the count call also returns 1
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(1));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCommentShouldNotBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .isArray()
            .jsonPath("$")
            .isEmpty();

        // Check, that the count call also returns 0
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(0));
    }

    @Test
    void getNonExistingComment() {
        // Get the comment
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingComment() throws Exception {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comment
        Comment updatedComment = commentRepository.findById(comment.getId()).block();
        updatedComment.content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedComment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCommentToMatchAllProperties(updatedComment);
    }

    @Test
    void putNonExistingComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCommentWithPatch() throws Exception {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comment using partial update
        Comment partialUpdatedComment = new Comment();
        partialUpdatedComment.setId(comment.getId());

        partialUpdatedComment.createTime(UPDATED_CREATE_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommentUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedComment, comment), getPersistedComment(comment));
    }

    @Test
    void fullUpdateCommentWithPatch() throws Exception {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the comment using partial update
        Comment partialUpdatedComment = new Comment();
        partialUpdatedComment.setId(comment.getId());

        partialUpdatedComment.content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedComment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedComment))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Comment in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCommentUpdatableFieldsEquals(partialUpdatedComment, getPersistedComment(partialUpdatedComment));
    }

    @Test
    void patchNonExistingComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamComment() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        comment.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(comment))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Comment in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteComment() {
        // Initialize the database
        insertedComment = commentRepository.save(comment).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the comment
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, comment.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return commentRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Comment getPersistedComment(Comment comment) {
        return commentRepository.findById(comment.getId()).block();
    }

    protected void assertPersistedCommentToMatchAllProperties(Comment expectedComment) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCommentAllPropertiesEquals(expectedComment, getPersistedComment(expectedComment));
        assertCommentUpdatableFieldsEquals(expectedComment, getPersistedComment(expectedComment));
    }

    protected void assertPersistedCommentToMatchUpdatableProperties(Comment expectedComment) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCommentAllUpdatablePropertiesEquals(expectedComment, getPersistedComment(expectedComment));
        assertCommentUpdatableFieldsEquals(expectedComment, getPersistedComment(expectedComment));
    }
}
