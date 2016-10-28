package ch.difty.sipamato.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import ch.difty.sipamato.entity.SipamatoEntity;
import ch.difty.sipamato.entity.SipamatoFilter;
import ch.difty.sipamato.lib.NullArgumentException;

/**
 * The generic {@link ReadOnlyService} interface, defining the common methods.
 *
 * @author u.joss
 *
 * @param <T> the entity type, extending {@link SipamatoEntity}
 * @param <ID> the type of the ID of entity <literal>T</literal>
 * @param <F> the filter, extending {@link SipamatoFilter}
 */
public interface ReadOnlyService<T extends SipamatoEntity, ID, F extends SipamatoFilter> extends Serializable {

    /**
     * Finds an individual entity by ID. Returns it as an optional of type <literal>T</literal>
     *
     * @param id - must not be null
     * @return Optional
     * @throws NullArgumentException if id is null
     */
    Optional<T> findById(ID id);

    /**
     * Finds a page of records of type <literal>T</literal> matching the provided filter.
     *
     * @param filter the filter
     * @param pageable defining paging and sorting
     * @return a list of entities of type <literal>T</literal>
     */
    List<T> findByFilter(F filter, Pageable pageable);

    /**
     * Counts the number of entities matching the specified filter.
     *
     * @param filter
     * @return entity count
     */
    int countByFilter(F filter);

}