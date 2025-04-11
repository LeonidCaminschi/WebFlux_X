package md.esempla.webflux.domain;

import static md.esempla.webflux.domain.PostStatusTestSamples.*;
import static md.esempla.webflux.domain.PostTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import md.esempla.webflux.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PostStatusTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PostStatus.class);
        PostStatus postStatus1 = getPostStatusSample1();
        PostStatus postStatus2 = new PostStatus();
        assertThat(postStatus1).isNotEqualTo(postStatus2);

        postStatus2.setId(postStatus1.getId());
        assertThat(postStatus1).isEqualTo(postStatus2);

        postStatus2 = getPostStatusSample2();
        assertThat(postStatus1).isNotEqualTo(postStatus2);
    }

    @Test
    void postTest() {
        PostStatus postStatus = getPostStatusRandomSampleGenerator();
        Post postBack = getPostRandomSampleGenerator();

        postStatus.setPost(postBack);
        assertThat(postStatus.getPost()).isEqualTo(postBack);
        assertThat(postBack.getPostStatus()).isEqualTo(postStatus);

        postStatus.post(null);
        assertThat(postStatus.getPost()).isNull();
        assertThat(postBack.getPostStatus()).isNull();
    }
}
