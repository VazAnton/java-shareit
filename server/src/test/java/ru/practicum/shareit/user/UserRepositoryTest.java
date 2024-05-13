package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "db.name=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    UserRepository userRepository;
    User user1 = new User(1L, "user1", "user@user.com");

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    public void checkSaveEntityInDb() {
        User savedUser = userRepository.save(user1);

        assertEquals(savedUser, userRepository.findById(savedUser.getId()).get());
    }
}
