package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(properties = "db.name=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UserRepository userRepository;

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    public void checkSaveItemInDb() {
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);

        Item savedItem = itemRepository.save(item);

        assertNotNull(savedItem);
    }

    @Test
    public void checkFindAllByOwnerIdOrderByIdDesc() {
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        item.setOwner(user);
        Pageable pageable = PageRequest.of(0, 10);

        itemRepository.save(item);

        assertEquals(1, itemRepository.findAllByOwnerIdOrderByIdDesc(user.getId(), pageable).getContent().size());
    }

    @Test
    public void checkSearchByText() {
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        itemRepository.save(item);

        assertEquals(1, itemRepository.searchByText("Кух").size());
    }

    @Test
    public void checkSearchByTextLikePage() {
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        itemRepository.save(item);
        Pageable pageable = PageRequest.of(0, 10);

        assertEquals(1, itemRepository.searchByTextLikePage("Кух", pageable).getContent().size());
    }

    @Test
    public void checkFindAllByOwnerId() {
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        item.setOwner(user);

        itemRepository.save(item);

        assertEquals(1, itemRepository.findAllByOwnerId(user.getId()).size());
    }
}
