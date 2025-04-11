package md.esempla.webflux.web.rest;

import static md.esempla.webflux.domain.PostStatusAsserts.*;
import static md.esempla.webflux.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import md.esempla.webflux.IntegrationTest;
import md.esempla.webflux.domain.PostStatus;
import md.esempla.webflux.repository.EntityManager;
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
 * Integration tests for the {@link PostStatusResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PostStatusResourceIT {

    private static final String DEFAULT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_STATUS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/post-statuses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PostStatusRepository postStatusRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private PostStatus postStatus;

    private PostStatus insertedPostStatus;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PostStatus createEntity() {
        return new PostStatus().status(DEFAULT_STATUS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PostStatus createUpdatedEntity() {
        return new PostStatus().status(UPDATED_STATUS);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(PostStatus.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void initTest() {
        postStatus = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedPostStatus != null) {
            postStatusRepository.delete(insertedPostStatus).block();
            insertedPostStatus = null;
        }
        deleteEntities(em);
    }

    @Test
    void createPostStatus() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the PostStatus
        var returnedPostStatus = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(PostStatus.class)
            .returnResult()
            .getResponseBody();

        // Validate the PostStatus in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPostStatusUpdatableFieldsEquals(returnedPostStatus, getPersistedPostStatus(returnedPostStatus));

        insertedPostStatus = returnedPostStatus;
    }

    @Test
    void createPostStatusWithExistingId() throws Exception {
        // Create the PostStatus with an existing ID
        postStatus.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        postStatus.setStatus(null);

        // Create the PostStatus, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllPostStatusesAsStream() {
        // Initialize the database
        postStatusRepository.save(postStatus).block();

        List<PostStatus> postStatusList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(PostStatus.class)
            .getResponseBody()
            .filter(postStatus::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(postStatusList).isNotNull();
        assertThat(postStatusList).hasSize(1);
        PostStatus testPostStatus = postStatusList.get(0);

        // Test fails because reactive api returns an empty object instead of null
        // assertPostStatusAllPropertiesEquals(postStatus, testPostStatus);
        assertPostStatusUpdatableFieldsEquals(postStatus, testPostStatus);
    }

    @Test
    void getAllPostStatuses() {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        // Get all the postStatusList
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
            .value(hasItem(postStatus.getId().intValue()))
            .jsonPath("$.[*].status")
            .value(hasItem(DEFAULT_STATUS));
    }

    @Test
    void getPostStatus() {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        // Get the postStatus
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, postStatus.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(postStatus.getId().intValue()))
            .jsonPath("$.status")
            .value(is(DEFAULT_STATUS));
    }

    @Test
    void getNonExistingPostStatus() {
        // Get the postStatus
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingPostStatus() throws Exception {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the postStatus
        PostStatus updatedPostStatus = postStatusRepository.findById(postStatus.getId()).block();
        updatedPostStatus.status(UPDATED_STATUS);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPostStatus.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedPostStatus))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPostStatusToMatchAllProperties(updatedPostStatus);
    }

    @Test
    void putNonExistingPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, postStatus.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePostStatusWithPatch() throws Exception {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the postStatus using partial update
        PostStatus partialUpdatedPostStatus = new PostStatus();
        partialUpdatedPostStatus.setId(postStatus.getId());

        partialUpdatedPostStatus.status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPostStatus.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPostStatus))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PostStatus in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostStatusUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPostStatus, postStatus),
            getPersistedPostStatus(postStatus)
        );
    }

    @Test
    void fullUpdatePostStatusWithPatch() throws Exception {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the postStatus using partial update
        PostStatus partialUpdatedPostStatus = new PostStatus();
        partialUpdatedPostStatus.setId(postStatus.getId());

        partialUpdatedPostStatus.status(UPDATED_STATUS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPostStatus.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPostStatus))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PostStatus in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPostStatusUpdatableFieldsEquals(partialUpdatedPostStatus, getPersistedPostStatus(partialUpdatedPostStatus));
    }

    @Test
    void patchNonExistingPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, postStatus.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPostStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        postStatus.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(postStatus))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the PostStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePostStatus() {
        // Initialize the database
        insertedPostStatus = postStatusRepository.save(postStatus).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the postStatus
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, postStatus.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return postStatusRepository.count().block();
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

    protected PostStatus getPersistedPostStatus(PostStatus postStatus) {
        return postStatusRepository.findById(postStatus.getId()).block();
    }

    protected void assertPersistedPostStatusToMatchAllProperties(PostStatus expectedPostStatus) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPostStatusAllPropertiesEquals(expectedPostStatus, getPersistedPostStatus(expectedPostStatus));
        assertPostStatusUpdatableFieldsEquals(expectedPostStatus, getPersistedPostStatus(expectedPostStatus));
    }

    protected void assertPersistedPostStatusToMatchUpdatableProperties(PostStatus expectedPostStatus) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPostStatusAllUpdatablePropertiesEquals(expectedPostStatus, getPersistedPostStatus(expectedPostStatus));
        assertPostStatusUpdatableFieldsEquals(expectedPostStatus, getPersistedPostStatus(expectedPostStatus));
    }
}
