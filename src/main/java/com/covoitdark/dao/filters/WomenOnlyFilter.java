package com.covoitdark.dao.filters;

public class WomenOnlyFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.women_only = 1 ");
    }
}
