package com.covoitdark.dao.filters;

public class AcFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.ac_available = 1 ");
    }
}
