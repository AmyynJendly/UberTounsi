package com.covoitdark.dao.filters;

/**
 * Filters trips by luggage policy (NONE, SMALL, LARGE).
 * Open/Closed Principle: new filter added without touching TripDAO.
 */
public class LuggageFilter implements TripFilter {
    private final String policy; // "NONE" | "SMALL" | "LARGE"
    public LuggageFilter(String policy) { this.policy = policy.toUpperCase(); }
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.luggage_policy = '").append(policy).append("' ");
    }
}
