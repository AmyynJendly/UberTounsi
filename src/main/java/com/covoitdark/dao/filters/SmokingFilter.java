package com.covoitdark.dao.filters;

public class SmokingFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.smoking_allowed = 1 ");
    }
}
