package md.esempla.webflux.domain;

import static md.esempla.webflux.domain.CommentTestSamples.*;
import static md.esempla.webflux.domain.PostStatusTestSamples.*;
import static md.esempla.webflux.domain.PostTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import md.esempla.webflux.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Post.class);
        Post post1 = getPostSample1();
        Post post2 = new Post();
        assertThat(post1).isNotEqualTo(post2);

        post2.setId(post1.getId());
        assertThat(post1).isEqualTo(post2);

        post2 = getPostSample2();
        assertThat(post1).isNotEqualTo(post2);
    }

    @Test
    void postStatusTest() {
        Post post = getPostRandomSampleGenerator();
        PostStatus postStatusBack = getPostStatusRandomSampleGenerator();

        post.setPostStatus(postStatusBack);
        assertThat(post.getPostStatus()).isEqualTo(postStatusBack);

        post.postStatus(null);
        assertThat(post.getPostStatus()).isNull();
    }

    @Test
    void commentTest() {
        Post post = getPostRandomSampleGenerator();
        Comment commentBack = getCommentRandomSampleGenerator();

        post.addComment(commentBack);
        assertThat(post.getComments()).containsOnly(commentBack);
        assertThat(commentBack.getPost()).isEqualTo(post);

        post.removeComment(commentBack);
        assertThat(post.getComments()).doesNotContain(commentBack);
        assertThat(commentBack.getPost()).isNull();

        post.comments(new HashSet<>(Set.of(commentBack)));
        assertThat(post.getComments()).containsOnly(commentBack);
        assertThat(commentBack.getPost()).isEqualTo(post);

        post.setComments(new HashSet<>());
        assertThat(post.getComments()).doesNotContain(commentBack);
        assertThat(commentBack.getPost()).isNull();
    }
}
