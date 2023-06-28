package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    @Query(value = "select * from bookings " +
            "where item_id = ?1 and start_time < current_timestamp and status != 'REJECTED' " +
            "order by start_time desc limit 1", nativeQuery = true)
    Booking findLastItemBooking(long itemId);

    @Query(value = "select * from bookings " +
            "where item_id = ?1 and start_time > current_timestamp and status != 'REJECTED' " +
            "order by start_time limit 1", nativeQuery = true)
    Booking findNextItemBooking(long itemId);

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndEndBefore(
            long itemId, long bookerId, BookingStatus status, LocalDateTime endTime);
}
