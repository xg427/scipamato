package ch.difty.scipamato.core.persistence;

import java.util.List;

import ch.difty.scipamato.common.persistence.paging.PaginationContext;
import ch.difty.scipamato.core.entity.SearchOrder;
import ch.difty.scipamato.core.entity.filter.PaperFilter;
import ch.difty.scipamato.core.entity.projection.PaperSlim;

/**
 * The {@link PaperSlimService} interface - defining {@link PaperSlim} specific
 * service methods.
 *
 * @author u.joss
 */
public interface PaperSlimService extends ReadOnlyService<Long, PaperSlim, PaperFilter> {

    /**
     * Find any paper matching the provided {@link SearchOrder}.
     *
     * @param searchOrder
     *            {@link SearchOrder}
     * @return list of {@link PaperSlim}s
     */
    List<PaperSlim> findBySearchOrder(SearchOrder searchOrder);

    /**
     * Finds a page full of {@link PaperSlim}s matching the provided
     * {@link SearchOrder} and {@link PaginationContext}
     *
     * @param searchOrder
     *            the filter
     * @param paginationContext
     *            context defining paging and sorting
     * @return paged list of papers
     */
    List<PaperSlim> findPageBySearchOrder(SearchOrder searchOrder, PaginationContext paginationContext);

    /**
     * Counts the number of entities matching the specified {@link SearchOrder}.
     *
     * @param searchOrder
     *            {@link SearchOrder}
     * @return paper count
     */
    int countBySearchOrder(SearchOrder searchOrder);

}
