package com.covoitdark.dao.filters;

/**
 * Filters trips that have at least {@code minSeats} available seats.
 * Open/Closed Principle: new filter added without touching TripDAO.
 */
public class MinSeatsFilter implements TripFilter {
    private final int minSeats;
    public MinSeatsFilter(int minSeats) { this.minSeats = Math.max(1, minSeats); }
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.available_seats >= ").append(minSeats).append(" ");
    }
}
