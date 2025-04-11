package md.esempla.webflux.web.rest;

import static md.esempla.webflux.domain.PostAsserts.*;
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
import md.esempla.webflux.domain.Post;
import md.esempla.webflux.domain.PostStatus;
import md.esempla.webflux.repository.EntityManager;
import md.esempla.webflux.repository.PostRepository;
import md.esempla.webflux.repository.PostStatusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PostResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PostResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final ZonedDateTime DEFAULT_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_UPDATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/posts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Post post;

    private Post insertedPost;

    @Autowired
    private PostStatusRepository postStatusRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createEntity() {
        return new Post().title(DEFAULT_TITLE).content(DEFAULT_CONTENT).createTime(DEFAULT_CREATE_TIME).updateTime(DEFAULT_UPDATE_TIME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createUpdatedEntity() {
        return new Post().title(UPDATED_TITLE).content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME).updateTime(UPDATED_UPDATE_TIME);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Post.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        post = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedPost != null) {
            postRepository.delete(insertedPost).block();
            insertedPost = null;
        }
        deleteEntities(em);
    }

    @Test
    void createPost() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Post
        var returnedPost = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Post.class)
            .returnResult()
            .getResponseBody();

        // Validate the Post in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPostUpdatableFieldsEquals(returnedPost, getPersistedPost(returnedPost));

        insertedPost = returnedPost;
    }

    @Test
    void createPostWithExistingId() throws Exception {
        // Create the Post with an existing ID
        post.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        post.setTitle(null);

        // Create the Post, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCreateTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        post.setCreateTime(null);

        // Create the Post, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkUpdateTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        post.setUpdateTime(null);

        // Create the Post, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllPosts() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList
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
            .value(hasItem(post.getId().intValue()))
            .jsonPath("$.[*].title")
            .value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].content")
            .value(hasItem(DEFAULT_CONTENT))
            .jsonPath("$.[*].createTime")
            .value(hasItem(sameInstant(DEFAULT_CREATE_TIME)))
            .jsonPath("$.[*].updateTime")
            .value(hasItem(sameInstant(DEFAULT_UPDATE_TIME)));
    }

    @Test
    void getPost() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get the post
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(post.getId().intValue()))
            .jsonPath("$.title")
            .value(is(DEFAULT_TITLE))
            .jsonPath("$.content")
            .value(is(DEFAULT_CONTENT))
            .jsonPath("$.createTime")
            .value(is(sameInstant(DEFAULT_CREATE_TIME)))
            .jsonPath("$.updateTime")
            .value(is(sameInstant(DEFAULT_UPDATE_TIME)));
    }

    @Test
    void getPostsByIdFiltering() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        Long id = post.getId();

        defaultPostFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultPostFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultPostFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    void getAllPostsByTitleIsEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where title equals to
        defaultPostFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    void getAllPostsByTitleIsInShouldWork() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where title in
        defaultPostFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    void getAllPostsByTitleIsNullOrNotNull() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where title is not null
        defaultPostFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    void getAllPostsByTitleContainsSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where title contains
        defaultPostFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    void getAllPostsByTitleNotContainsSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where title does not contain
        defaultPostFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    void getAllPostsByContentIsEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where content equals to
        defaultPostFiltering("content.equals=" + DEFAULT_CONTENT, "content.equals=" + UPDATED_CONTENT);
    }

    @Test
    void getAllPostsByContentIsInShouldWork() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where content in
        defaultPostFiltering("content.in=" + DEFAULT_CONTENT + "," + UPDATED_CONTENT, "content.in=" + UPDATED_CONTENT);
    }

    @Test
    void getAllPostsByContentIsNullOrNotNull() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where content is not null
        defaultPostFiltering("content.specified=true", "content.specified=false");
    }

    @Test
    void getAllPostsByContentContainsSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where content contains
        defaultPostFiltering("content.contains=" + DEFAULT_CONTENT, "content.contains=" + UPDATED_CONTENT);
    }

    @Test
    void getAllPostsByContentNotContainsSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where content does not contain
        defaultPostFiltering("content.doesNotContain=" + UPDATED_CONTENT, "content.doesNotContain=" + DEFAULT_CONTENT);
    }

    @Test
    void getAllPostsByCreateTimeIsEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime equals to
        defaultPostFiltering("createTime.equals=" + DEFAULT_CREATE_TIME, "createTime.equals=" + UPDATED_CREATE_TIME);
    }

    @Test
    void getAllPostsByCreateTimeIsInShouldWork() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime in
        defaultPostFiltering("createTime.in=" + DEFAULT_CREATE_TIME + "," + UPDATED_CREATE_TIME, "createTime.in=" + UPDATED_CREATE_TIME);
    }

    @Test
    void getAllPostsByCreateTimeIsNullOrNotNull() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime is not null
        defaultPostFiltering("createTime.specified=true", "createTime.specified=false");
    }

    @Test
    void getAllPostsByCreateTimeIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime is greater than or equal to
        defaultPostFiltering(
            "createTime.greaterThanOrEqual=" + DEFAULT_CREATE_TIME,
            "createTime.greaterThanOrEqual=" + UPDATED_CREATE_TIME
        );
    }

    @Test
    void getAllPostsByCreateTimeIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime is less than or equal to
        defaultPostFiltering("createTime.lessThanOrEqual=" + DEFAULT_CREATE_TIME, "createTime.lessThanOrEqual=" + SMALLER_CREATE_TIME);
    }

    @Test
    void getAllPostsByCreateTimeIsLessThanSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime is less than
        defaultPostFiltering("createTime.lessThan=" + UPDATED_CREATE_TIME, "createTime.lessThan=" + DEFAULT_CREATE_TIME);
    }

    @Test
    void getAllPostsByCreateTimeIsGreaterThanSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where createTime is greater than
        defaultPostFiltering("createTime.greaterThan=" + SMALLER_CREATE_TIME, "createTime.greaterThan=" + DEFAULT_CREATE_TIME);
    }

    @Test
    void getAllPostsByUpdateTimeIsEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime equals to
        defaultPostFiltering("updateTime.equals=" + DEFAULT_UPDATE_TIME, "updateTime.equals=" + UPDATED_UPDATE_TIME);
    }

    @Test
    void getAllPostsByUpdateTimeIsInShouldWork() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime in
        defaultPostFiltering("updateTime.in=" + DEFAULT_UPDATE_TIME + "," + UPDATED_UPDATE_TIME, "updateTime.in=" + UPDATED_UPDATE_TIME);
    }

    @Test
    void getAllPostsByUpdateTimeIsNullOrNotNull() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime is not null
        defaultPostFiltering("updateTime.specified=true", "updateTime.specified=false");
    }

    @Test
    void getAllPostsByUpdateTimeIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime is greater than or equal to
        defaultPostFiltering(
            "updateTime.greaterThanOrEqual=" + DEFAULT_UPDATE_TIME,
            "updateTime.greaterThanOrEqual=" + UPDATED_UPDATE_TIME
        );
    }

    @Test
    void getAllPostsByUpdateTimeIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime is less than or equal to
        defaultPostFiltering("updateTime.lessThanOrEqual=" + DEFAULT_UPDATE_TIME, "updateTime.lessThanOrEqual=" + SMALLER_UPDATE_TIME);
    }

    @Test
    void getAllPostsByUpdateTimeIsLessThanSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime is less than
        defaultPostFiltering("updateTime.lessThan=" + UPDATED_UPDATE_TIME, "updateTime.lessThan=" + DEFAULT_UPDATE_TIME);
    }

    @Test
    void getAllPostsByUpdateTimeIsGreaterThanSomething() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        // Get all the postList where updateTime is greater than
        defaultPostFiltering("updateTime.greaterThan=" + SMALLER_UPDATE_TIME, "updateTime.greaterThan=" + DEFAULT_UPDATE_TIME);
    }

    @Test
    void getAllPostsByPostStatusIsEqualToSomething() {
        PostStatus postStatus = PostStatusResourceIT.createEntity();
        postStatusRepository.save(postStatus).block();
        Long postStatusId = postStatus.getId();
        post.setPostStatusId(postStatusId);
        insertedPost = postRepository.save(post).block();
        // Get all the postList where postStatus equals to postStatusId
        defaultPostShouldBeFound("postStatusId.equals=" + postStatusId);

        // Get all the postList where postStatus equals to (postStatusId + 1)
        defaultPostShouldNotBeFound("postStatusId.equals=" + (postStatusId + 1));
    }

    private void defaultPostFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultPostShouldBeFound(shouldBeFound);
        defaultPostShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPostShouldBeFound(String filter) {
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
            .value(hasItem(post.getId().intValue()))
            .jsonPath("$.[*].title")
            .value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].content")
            .value(hasItem(DEFAULT_CONTENT))
            .jsonPath("$.[*].createTime")
            .value(hasItem(sameInstant(DEFAULT_CREATE_TIME)))
            .jsonPath("$.[*].updateTime")
            .value(hasItem(sameInstant(DEFAULT_UPDATE_TIME)));

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
    private void defaultPostShouldNotBeFound(String filter) {
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
    void getNonExistingPost() {
        // Get the post
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingPost() throws Exception {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the post
        Post updatedPost = postRepository.findById(post.getId()).block();
        updatedPost.title(UPDATED_TITLE).content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME).updateTime(UPDATED_UPDATE_TIME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPost.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPostToMatchAllProperties(updatedPost);
    }

    @Test
    void putNonExistingPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, post.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePostWithPatch() throws Exception {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        partialUpdatedPost.content(UPDATED_CONTENT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPost.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPost, post), getPersistedPost(post));
    }

    @Test
    void fullUpdatePostWithPatch() throws Exception {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        partialUpdatedPost.title(UPDATED_TITLE).content(UPDATED_CONTENT).createTime(UPDATED_CREATE_TIME).updateTime(UPDATED_UPDATE_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPost.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPost))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Post in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostUpdatableFieldsEquals(partialUpdatedPost, getPersistedPost(partialUpdatedPost));
    }

    @Test
    void patchNonExistingPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, post.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPost() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        post.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(post))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Post in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePost() {
        // Initialize the database
        insertedPost = postRepository.save(post).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the post
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, post.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return postRepository.count().block();
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

    protected Post getPersistedPost(Post post) {
        return postRepository.findById(post.getId()).block();
    }

    protected void assertPersistedPostToMatchAllProperties(Post expectedPost) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPostAllPropertiesEquals(expectedPost, getPersistedPost(expectedPost));
        assertPostUpdatableFieldsEquals(expectedPost, getPersistedPost(expectedPost));
    }

    protected void assertPersistedPostToMatchUpdatableProperties(Post expectedPost) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPostAllUpdatablePropertiesEquals(expectedPost, getPersistedPost(expectedPost));
        assertPostUpdatableFieldsEquals(expectedPost, getPersistedPost(expectedPost));
    }
}
