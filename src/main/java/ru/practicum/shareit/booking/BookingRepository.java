package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT b " +
            "FROM Booking b " +
            "JOIN b.booker AS booker " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE b.id = ?1 " +
            "AND o.id = ?2 " +
            "OR b.id = ?1 " +
            "AND booker.id = ?2")
    Booking getBookingInfo(long bookingId, long userId);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1")
    List<Booking> getAllBookingsInfoByOwner(long userId);

    List<Booking> findAllByBookerId(long bookerId);

    List<Booking> findByItemId(long itemId);

    List<Booking> findAllByItemInAndStatusOrderByStartAsc(List<Item> items, Status status);

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1")
    Page<Booking> getAllBookingsInfoByOwnerLikePage(long userId, Pageable pageable);

    Page<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatusEquals(long bookerId, Status status, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndIsBefore(long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartIsBeforeOrderByIdAsc(long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartIsAfter(long bookerId, LocalDateTime now, Pageable pageable);
}
