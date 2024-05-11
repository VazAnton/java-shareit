package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "db.name=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    CommentRepository commentRepository;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    public void checkAddComment() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        itemRepository.save(item);
        Comment comment = new Comment(1L, "Add comment from user1", item, user, now);

        assertThat(commentRepository.save(comment))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(comment);
    }

    @Test
    public void checkFindAllByAuthorId() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        itemRepository.save(item);
        Comment comment = new Comment(1L, "Add comment from user1", item, user, now);

        commentRepository.save(comment);

        assertEquals(1, commentRepository.findAllByAuthorId(user.getId()).size());
    }

    @Test
    public void checkFindAllByItemId() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        itemRepository.save(item);
        Comment comment = new Comment(1L, "Add comment from user1", item, user, now);

        commentRepository.save(comment);

        assertEquals(1, commentRepository.findAllByItemId(item.getId()).size());
    }
}
