package com.covoitdark.dao.filters;

public class MusicFilter implements TripFilter {
    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.music_preference != 'NONE' ");
    }
}
