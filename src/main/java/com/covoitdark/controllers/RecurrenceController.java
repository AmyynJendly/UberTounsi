package com.covoitdark.controllers;

import com.covoitdark.dao.TripDAO;
import com.covoitdark.dao.NotificationDAO;
import com.covoitdark.models.Trip;
import com.covoitdark.models.Notification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feature 1 – Trip Recurrence.
 *
 * A trip with repeat_days (e.g. "MON,WED,FRI") and an end_date automatically
 * repeats on those days. This controller generates the list of upcoming occurrence
 * dates and can create notification summaries for matched saved-searches.
 *
 * Demonstrates:
 *  - Collections framework + Stream API: builds occurrence list via stream filter
 *  - Map: groups trips by driver using Collectors.groupingBy
 *  - Open/Closed Principle: new recurrence patterns extend without changing TripDAO
 */
public class RecurrenceController {

    private final TripDAO tripDAO = new TripDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    /** Day-name → DayOfWeek mapping (Collections Map). */
    private static final Map<String, DayOfWeek> DAY_MAP = Map.of(
        "MON", DayOfWeek.MONDAY,
        "TUE", DayOfWeek.TUESDAY,
        "WED", DayOfWeek.WEDNESDAY,
        "THU", DayOfWeek.THURSDAY,
        "FRI", DayOfWeek.FRIDAY,
        "SAT", DayOfWeek.SATURDAY,
        "SUN", DayOfWeek.SUNDAY
    );

    /**
     * Return all dates between startDate and endDate (inclusive) on which this
     * trip should run based on its repeat_days list.
     * Stream: filters a date range to matching days-of-week.
     */
    public List<LocalDate> getOccurrences(Trip trip) {
        List<LocalDate> occurrences = new ArrayList<>();
        if (trip.getRepeatDays() == null || trip.getRepeatDays().isEmpty()) {
            occurrences.add(trip.getStartDate());
            return occurrences;
        }
        LocalDate end = trip.getEndDate() != null ? trip.getEndDate() : trip.getStartDate().plusMonths(3);

        // Stream: iterate date range and keep only matching week-days
        List<DayOfWeek> activeDays = trip.getRepeatDays().stream()
                .map(d -> DAY_MAP.getOrDefault(d.trim().toUpperCase(), null))
                .filter(d -> d != null)
                .collect(Collectors.toList());

        LocalDate cursor = trip.getStartDate();
        while (!cursor.isAfter(end)) {
            if (activeDays.contains(cursor.getDayOfWeek())) {
                occurrences.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return occurrences;
    }

    /**
     * Returns upcoming occurrences (today or later) for a recurring trip.
     */
    public List<LocalDate> getUpcomingOccurrences(Trip trip) {
        return getOccurrences(trip).stream()
                .filter(d -> !d.isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }

    /**
     * Groups all active recurring trips by driver id.
     * Demonstrates Collectors.groupingBy (Map + Stream).
     */
    public Map<Integer, List<Trip>> getRecurringTripsByDriver() {
        return tripDAO.findAllActive().stream()
                .filter(t -> t.getRepeatDays() != null && !t.getRepeatDays().isEmpty())
                .collect(Collectors.groupingBy(Trip::getDriverId));
    }

    /**
     * Sends a notification to userId about an upcoming occurrence of trip.
     */
    public void notifyUpcomingOccurrence(int userId, Trip trip, LocalDate date) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle("Rappel trajet récurrent");
        n.setMessage("Votre trajet " + trip.getDeparture() + " → " + trip.getArrival()
                + " est prévu le " + date + " à " + trip.getDepartureTime() + ".");
        notificationDAO.create(n);
    }
}
