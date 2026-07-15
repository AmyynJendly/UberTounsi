package com.covoitdark.dao.filters;

/**
 * Filters trips within a price range [minPrice, maxPrice].
 * Open/Closed Principle: new filter added without touching TripDAO.
 */
public class PriceRangeFilter implements TripFilter {
    private final double minPrice;
    private final double maxPrice;

    public PriceRangeFilter(double minPrice, double maxPrice) {
        this.minPrice = Math.max(0, minPrice);
        this.maxPrice = Math.max(minPrice, maxPrice);
    }

    @Override
    public void apply(StringBuilder sql) {
        sql.append("AND t.price >= ").append(minPrice)
           .append(" AND t.price <= ").append(maxPrice).append(" ");
    }
}
