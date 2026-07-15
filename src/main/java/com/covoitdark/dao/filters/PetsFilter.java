package com.covoitdark.dao.filters;

public class PetsFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.pets_allowed = 1 ");
    }
}
