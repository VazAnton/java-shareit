package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "db.name=test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingRepositoryTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ItemRepository itemRepository;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    public void checkAddSaveBooking() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        userRepository.save(user);
        itemRepository.save(item);
        Booking booking = new Booking(1L, now, now.minusHours(5L), item, user, Status.WAITING);

        assertThat(bookingRepository.save(booking))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }

    @Test
    public void checkGetBookingInfo() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        item.setOwner(user);
        userRepository.save(user);
        itemRepository.save(item);
        Booking booking = new Booking(1L, now, now.minusHours(5L), item, user, Status.WAITING);
        bookingRepository.save(booking);

        assertThat(bookingRepository.getBookingInfo(booking.getId(), user.getId()))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }

    @Test
    public void checkGetAllBookingsInfoByOwner() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        item.setOwner(user);
        userRepository.save(user);
        itemRepository.save(item);
        Booking booking = new Booking(1L, now, now.minusHours(5L), item, user, Status.WAITING);
        bookingRepository.save(booking);

        assertEquals(1, bookingRepository.getAllBookingsInfoByOwner(user.getId()).size());
    }

    @Test
    public void checkGetAllBookingsInfoByOwnerLikePage() {
        LocalDateTime now = LocalDateTime.now();
        Item item = new Item(1L,
                "Кухонный стол",
                "Стол для празднования",
                true);
        User user = new User(1L, "user1", "user@user.com");
        Pageable pageable = PageRequest.of(0, 10);
        item.setOwner(user);
        userRepository.save(user);
        itemRepository.save(item);
        Booking booking = new Booking(1L, now, now.minusHours(5L), item, user, Status.WAITING);
        bookingRepository.save(booking);

        assertEquals(1, bookingRepository.getAllBookingsInfoByOwnerLikePage(user.getId(), pageable)
                .getContent().size());
    }
}
