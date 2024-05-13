package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "db.name=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class RequestRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    RequestRepository requestRepository;
    @Autowired
    UserRepository userRepository;
    User user1 = new User(1L, "user1", "user@user.com");
    ItemRequest itemRequest = new ItemRequest(0L, "Хотел бы воспользоваться щёткой для обуви", null,
            LocalDateTime.now());

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    public void checkSaveEntityInDb() {
        User savedUser = userRepository.save(user1);
        itemRequest.setRequester(savedUser);
        requestRepository.save(itemRequest);

        Assertions.assertNotNull(itemRequest.getId());
    }

    @Test
    public void checkFindAllByRequesterIdOrderByCreatedDesc() {
        User savedUser = userRepository.save(user1);
        itemRequest.setRequester(savedUser);
        requestRepository.save(itemRequest);

        assertEquals(1, requestRepository.findAllByRequesterIdOrderByCreatedDesc(savedUser.getId()).size());
    }
}
