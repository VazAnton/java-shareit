package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @MockBean
    BookingService bookingService;
    @Mock
    BookingRepository bookingRepository;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    ShortBookingDto shortBookingDto = new ShortBookingDto(
            0L,
            LocalDateTime.of(2024, 5, 25, 20, 30),
            LocalDateTime.of(2024, 5, 25, 22, 50));
    User user1 = new User(1L, "user1", "user@user.com");
    Item item = new Item(1L, "Кухонный стол", "Стол для празднования", true);
    Booking bookingBeforePatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
            Status.WAITING);
    Booking bookingAfterPatch = new Booking(1L, shortBookingDto.getStart(), shortBookingDto.getEnd(), item, user1,
            Status.APPROVED);

    @Test
    void checkAddBooking() throws Exception {
        when(bookingService.addBooking(shortBookingDto, user1.getId()))
                .thenReturn(bookingBeforePatch);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", user1.getId())
                        .content(mapper.writeValueAsString(shortBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, times(1)).addBooking(shortBookingDto, user1.getId());
    }

    @Test
    void checkUpdateBooking() throws Exception {
        when(bookingService.updateBooking(bookingBeforePatch.getId(), user1.getId(), "true"))
                .thenReturn(bookingAfterPatch);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingBeforePatch.getId())
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, times(1))
                .updateBooking(bookingBeforePatch.getId(), user1.getId(), "true");
    }

    @Test
    void checkDeleteBooking() throws Exception {
        mockMvc.perform(delete("/bookings/{id}", 1L))
                .andExpect(status().isOk()).andReturn();

        verify(bookingService, times(1)).deleteBooking(1L);
    }

    @Test
    void checkGetBooking() throws Exception {
        when(bookingService.getBooking(bookingAfterPatch.getId(), user1.getId()))
                .thenReturn(bookingAfterPatch);

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", user1.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, times(1)).getBooking(bookingAfterPatch.getId(), user1.getId());
    }

    @Test
    void checkGetBookings() throws Exception {
        when(bookingService.getBookingsByUser(user1.getId(), "ALL", 0, 20))
                .thenReturn(List.of(bookingAfterPatch));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, times(1))
                .getBookingsByUser(user1.getId(), "ALL", 0, 20);
    }

    @Test
    public void checkGetBookingsIfStateIsCurrent() throws Exception {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking(4L, LocalDateTime.of(2023, 5, 5, 23, 30),
                LocalDateTime.of(2025, 6, 6, 20, 15), item, user1, Status.APPROVED));
        bookings.add(new Booking(3L, LocalDateTime.of(2024, 5, 5, 23, 30),
                LocalDateTime.of(2026, 6, 6, 20, 15), item, user1, Status.APPROVED));
        Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
        Pageable pageable = PageRequest.of(0, 10, sortByStart);
        Page<Booking> pagedBookings = new PageImpl<>(bookings);
        when(bookingRepository.findAllByBookerIdAndStartIsBeforeOrderByIdAsc(user1.getId(), LocalDateTime.now(), pageable))
                .thenReturn(pagedBookings);
        when(bookingService.getBookingsByUser(user1.getId(), "CURRENT", 0, 10))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("state", "CURRENT")
                        .param("from", String.valueOf(0))
                        .param("size", String.valueOf(10))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookings)));

        assertEquals(2, bookingService.getBookingsByUser(user1.getId(), "CURRENT", 0, 10).size());
    }

    @Test
    void checkGetBookingsByOwner() throws Exception {
        when(bookingService.getBookingsByOwner(user1.getId(), "ALL", 0, 20))
                .thenReturn(List.of(bookingAfterPatch));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", user1.getId())
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService, times(1))
                .getBookingsByOwner(user1.getId(), "ALL", 0, 20);
    }
}
