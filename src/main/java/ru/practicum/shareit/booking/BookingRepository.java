package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and current_timestamp between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findCurrentByBookerId(long bookerId);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and b.end < current_timestamp " +
            "order by b.start desc")
    List<Booking> findPastByBookerId(long bookerId);

    @Query(value = "select b from Booking b " +
            "where b.booker.id = ?1 and b.start > current_timestamp " +
            "order by b.start desc")
    List<Booking> findFutureByBookerId(long bookerId);

    @Query(value = "select b from Booking b " +
            "where b.item.ownerId = ?1 " +
            "order by b.start desc")
    List<Booking> findByOwnerId(long ownerId);

    @Query(value = "select b from Booking b " +
            "where b.item.ownerId = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerIdAndStatus(long ownerId, BookingStatus status);

    @Query(value = "select b from Booking b " +
            "where b.item.ownerId = ?1 and current_timestamp between b.start and b.end " +
            "order by b.start desc")
    List<Booking> findCurrentByOwnerId(long ownerId);

    @Query(value = "select b from Booking b " +
            "where b.item.ownerId = ?1 and b.end < current_timestamp " +
            "order by b.start desc")
    List<Booking> findPastByOwnerId(long ownerId);

    @Query(value = "select b from Booking b " +
            "where b.item.ownerId = ?1 and b.start > current_timestamp " +
            "order by b.start desc")
    List<Booking> findFutureByOwnerId(long ownerId);

    @Query(value = "select * from bookings " +
            "where item_id = ?1 and start_time < current_timestamp and status != ?2 " +
            "order by start_time desc limit 1", nativeQuery = true)
    Booking findLastItemBooking(long itemId, String status);

    @Query(value = "select * from bookings " +
            "where item_id = ?1 and start_time > current_timestamp and status != ?2 " +
            "order by start_time limit 1", nativeQuery = true)
    Booking findNextItemBooking(long itemId, String status);

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndEndBefore(
            long itemId, long bookerId, BookingStatus status, LocalDateTime endTime);
}
