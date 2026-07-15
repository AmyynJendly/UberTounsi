package com.covoitdark.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface — satisfies:
 *  - SOLID / Interface Segregation Principle (ISP): each DAO implements only what it needs
 *  - SOLID / Dependency Inversion Principle (DIP): handlers depend on this abstraction, not concrete DAOs
 *  - Java Generics: T is the entity type, ID is the primary key type
 *
 * @param <T>  the entity type (e.g. User, Trip, Car)
 * @param <ID> the primary key type (e.g. Integer)
 */
public interface IRepository<T, ID> {

    /** Persist a new entity and return true if successful. */
    boolean create(T entity);

    /** Find a single entity by its primary key, wrapped in Optional to avoid null. */
    Optional<T> findById(int id);

    /** Return all entities of this type. */
    List<T> findAll();

    /** Update an existing entity. */
    boolean update(T entity);
}
