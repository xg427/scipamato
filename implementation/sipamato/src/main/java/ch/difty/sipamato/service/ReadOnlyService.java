package ch.difty.sipamato.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import ch.difty.sipamato.entity.IdSipamatoEntity;
import ch.difty.sipamato.entity.SipamatoEntity;
import ch.difty.sipamato.entity.filter.SipamatoFilter;
import ch.difty.sipamato.lib.NullArgumentException;
import ch.difty.sipamato.paging.PaginationContext;

/**
 * The generic {@link ReadOnlyService} interface, defining the common methods.
 *
 * @author u.joss
 *
 * @param <ID> the type of the ID of entity {@code T}
 * @param <T> the entity type, extending {@link SipamatoEntity}
 * @param <F> the filter, extending {@link SipamatoFilter}
 */
public interface ReadOnlyService<ID extends Number, T extends IdSipamatoEntity<ID>, F extends SipamatoFilter> extends Serializable {

    /**
     * Finds an individual entity by ID. Returns it as an optional of type {@code T}
     *
     * @param id - must not be null
     * @return Optional
     * @throws NullArgumentException if id is null
     */
    Optional<T> findById(ID id);

    /**
     * Finds a page full of records of type {@code T} matching the provided filter and pagination context.
     *
     * @param filter the filter
     * @param pagination context defining paging and sorting
     * @return a page of entities of type {@code T} as list
     */
    List<T> findPageByFilter(F filter, PaginationContext paginationContext);

    /**
     * Counts the number of entities matching the specified filter.
     *
     * @param filter
     * @return entity count
     */
    int countByFilter(F filter);

}
