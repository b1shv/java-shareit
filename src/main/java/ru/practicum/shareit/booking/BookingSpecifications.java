package ru.practicum.shareit.booking;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.Item_;

import javax.persistence.criteria.Join;
import java.time.LocalDateTime;

public class BookingSpecifications {
    private BookingSpecifications() {
    }

    public static Specification<Booking> bookerId(long bookerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Booking_.BOOKER), bookerId);
    }

    public static Specification<Booking> ownerId(long ownerId) {
        return (root, query, criteriaBuilder) -> {
            Join<Item, Booking> bookingItem = root.join(Booking_.ITEM);
            return criteriaBuilder.equal(bookingItem.get(Item_.OWNER_ID), ownerId);
        };
    }

    public static Specification<Booking> status(BookingStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Booking_.STATUS), status);
    }

    public static Specification<Booking> startInPast() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(Booking_.START), LocalDateTime.now());
    }

    public static Specification<Booking> startInFuture() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(Booking_.START), LocalDateTime.now());
    }

    public static Specification<Booking> endInPast() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get(Booking_.END), LocalDateTime.now());
    }

    public static Specification<Booking> endInFuture() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get(Booking_.END), LocalDateTime.now());
    }
}
