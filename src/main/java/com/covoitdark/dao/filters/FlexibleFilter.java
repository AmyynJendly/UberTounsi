package com.covoitdark.dao.filters;

public class FlexibleFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.flexible_pickup = 1 ");
    }
}
