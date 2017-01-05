package ch.difty.sipamato.persistance.jooq.search;

import static ch.difty.sipamato.db.tables.Code.CODE;
import static ch.difty.sipamato.db.tables.CodeClass.CODE_CLASS;
import static ch.difty.sipamato.db.tables.CodeClassTr.CODE_CLASS_TR;
import static ch.difty.sipamato.db.tables.CodeTr.CODE_TR;
import static ch.difty.sipamato.db.tables.SearchCondition.SEARCH_CONDITION;
import static ch.difty.sipamato.db.tables.SearchConditionCode.SEARCH_CONDITION_CODE;
import static ch.difty.sipamato.db.tables.SearchExclusion.SEARCH_EXCLUSION;
import static ch.difty.sipamato.db.tables.SearchOrder.SEARCH_ORDER;
import static ch.difty.sipamato.db.tables.SearchTerm.SEARCH_TERM;
import static org.jooq.impl.DSL.row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Condition;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep4;
import org.jooq.InsertValuesStep6;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.difty.sipamato.db.tables.records.SearchConditionCodeRecord;
import ch.difty.sipamato.db.tables.records.SearchConditionRecord;
import ch.difty.sipamato.db.tables.records.SearchOrderRecord;
import ch.difty.sipamato.db.tables.records.SearchTermRecord;
import ch.difty.sipamato.entity.Code;
import ch.difty.sipamato.entity.SearchOrder;
import ch.difty.sipamato.entity.filter.BooleanSearchTerm;
import ch.difty.sipamato.entity.filter.IntegerSearchTerm;
import ch.difty.sipamato.entity.filter.SearchCondition;
import ch.difty.sipamato.entity.filter.SearchTerm;
import ch.difty.sipamato.entity.filter.StringSearchTerm;
import ch.difty.sipamato.lib.DateTimeService;
import ch.difty.sipamato.lib.TranslationUtils;
import ch.difty.sipamato.persistance.jooq.GenericFilterConditionMapper;
import ch.difty.sipamato.persistance.jooq.InsertSetStepSetter;
import ch.difty.sipamato.persistance.jooq.JooqEntityRepo;
import ch.difty.sipamato.persistance.jooq.JooqSortMapper;
import ch.difty.sipamato.persistance.jooq.UpdateSetStepSetter;
import ch.difty.sipamato.service.Localization;

/**
 * The repository to manage {@link SearchOrder}s - including the nested list of {@link SearchCondition}s and excluded paper ids.
 *
 * @author u.joss
 */
@Repository
public class JooqSearchOrderRepo extends JooqEntityRepo<SearchOrderRecord, SearchOrder, Long, ch.difty.sipamato.db.tables.SearchOrder, SearchOrderRecordMapper, SearchOrderFilter>
        implements SearchOrderRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqSearchOrderRepo.class);

    @Autowired
    public JooqSearchOrderRepo(DSLContext dsl, SearchOrderRecordMapper mapper, JooqSortMapper<SearchOrderRecord, SearchOrder, ch.difty.sipamato.db.tables.SearchOrder> sortMapper,
            GenericFilterConditionMapper<SearchOrderFilter> filterConditionMapper, DateTimeService dateTimeService, Localization localization,
            InsertSetStepSetter<SearchOrderRecord, SearchOrder> insertSetStepSetter, UpdateSetStepSetter<SearchOrderRecord, SearchOrder> updateSetStepSetter, Configuration jooqConfig) {
        super(dsl, mapper, sortMapper, filterConditionMapper, dateTimeService, localization, insertSetStepSetter, updateSetStepSetter, jooqConfig);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected Class<? extends SearchOrder> getEntityClass() {
        return SearchOrder.class;
    }

    @Override
    protected Class<? extends SearchOrderRecord> getRecordClass() {
        return SearchOrderRecord.class;
    }

    @Override
    protected ch.difty.sipamato.db.tables.SearchOrder getTable() {
        return SEARCH_ORDER;
    }

    @Override
    protected TableField<SearchOrderRecord, Long> getTableId() {
        return SEARCH_ORDER.ID;
    }

    @Override
    protected Long getIdFrom(SearchOrderRecord record) {
        return record.getId();
    }

    @Override
    protected Long getIdFrom(SearchOrder entity) {
        return entity.getId();
    }

    /**
     * Enriches the plain {@link SearchOrder} with nested entities, i.e. the {@link SearchCondition}s.
     */
    @Override
    protected void enrichAssociatedEntitiesOf(final SearchOrder searchOrder) {
        if (searchOrder != null && searchOrder.getId() != null) {
            fillSearchTermsInto(searchOrder, mapSearchTermsToSearchConditions(searchOrder));
            addSearchTermLessConditionsOf(searchOrder);
            fillExcludedPaperIdsInto(searchOrder);
            fillCodesIntoSearchConditionsOf(searchOrder);
        }
    }

    private Map<Long, List<SearchTerm<?>>> mapSearchTermsToSearchConditions(final SearchOrder searchOrder) {
        final List<SearchTerm<?>> searchTerms = fetchSearchTermsForSearchOrderWithId(searchOrder.getId());
        return searchTerms.stream().collect(Collectors.groupingBy(st -> st.getSearchConditionId()));
    }

    protected List<SearchTerm<?>> fetchSearchTermsForSearchOrderWithId(final long searchOrderId) {
        // @formatter:off
        return getDsl()
                .select(
                        SEARCH_TERM.ID.as("id"),
                        SEARCH_TERM.SEARCH_TERM_TYPE.as("stt"),
                        SEARCH_TERM.SEARCH_CONDITION_ID.as("scid"),
                        SEARCH_TERM.FIELD_NAME.as("fn"),
                        SEARCH_TERM.RAW_VALUE.as("rv"))
                .from(SEARCH_TERM)
                .innerJoin(SEARCH_CONDITION)
                .on(SEARCH_CONDITION.SEARCH_CONDITION_ID.equal(SEARCH_TERM.SEARCH_CONDITION_ID))
                .where(SEARCH_CONDITION.SEARCH_ORDER_ID.equal(searchOrderId))
                .fetch(r -> SearchTerm.of((long) r.get("id"), (int) r.get("stt"), (long) r.get("scid"), (String) r.get("fn"), (String) r.get("rv")));
        // @formatter:on
    }

    /*
     * Note: This method only adds searchConditions that have searchTerms. It will not add conditions that e.g. only have createdTerms or modifiedTerms.
     */
    private void fillSearchTermsInto(SearchOrder searchOrder, Map<Long, List<SearchTerm<?>>> map) {
        for (final Entry<Long, List<SearchTerm<?>>> entry : map.entrySet()) {
            final SearchCondition sc = new SearchCondition(entry.getKey());
            for (final SearchTerm<?> st : entry.getValue()) {
                sc.addSearchTerm(st);
            }
            searchOrder.add(sc);
        }
        enrichSearchConditionsOf(searchOrder);
    }

    private Map<Long, List<SearchTerm<?>>> mapSearchTermsToSearchConditions(final SearchCondition searchCondition) {
        final List<SearchTerm<?>> searchTerms = fetchSearchTermsForSearchConditionWithId(searchCondition.getSearchConditionId());
        return searchTerms.stream().collect(Collectors.groupingBy(st -> st.getSearchConditionId()));
    }

    protected List<SearchTerm<?>> fetchSearchTermsForSearchConditionWithId(final long searchConditionId) {
        // @formatter:off
        return getDsl()
                .select(
                        SEARCH_TERM.ID.as("id"),
                        SEARCH_TERM.SEARCH_TERM_TYPE.as("stt"),
                        SEARCH_TERM.SEARCH_CONDITION_ID.as("scid"),
                        SEARCH_TERM.FIELD_NAME.as("fn"),
                        SEARCH_TERM.RAW_VALUE.as("rv"))
                .from(SEARCH_TERM)
                .innerJoin(SEARCH_CONDITION)
                .on(SEARCH_CONDITION.SEARCH_CONDITION_ID.equal(SEARCH_TERM.SEARCH_CONDITION_ID))
                .where(SEARCH_CONDITION.SEARCH_CONDITION_ID.equal(searchConditionId))
                .fetch(r -> SearchTerm.of((long) r.get("id"), (int) r.get("stt"), (long) r.get("scid"), (String) r.get("fn"), (String) r.get("rv")));
        // @formatter:on
    }

    private void fillSearchTermsInto(SearchCondition searchCondition, Map<Long, List<SearchTerm<?>>> map) {
        for (final Entry<Long, List<SearchTerm<?>>> entry : map.entrySet()) {
            for (final SearchTerm<?> st : entry.getValue()) {
                searchCondition.addSearchTerm(st);
            }
        }
    }

    private void enrichSearchConditionsOf(final SearchOrder searchOrder) {
        if (searchOrder.getSearchConditions() != null) {
            for (final SearchCondition sc : searchOrder.getSearchConditions()) {
                if (sc != null && sc.getSearchConditionId() != null) {
                    final SearchCondition persisted = fetchSearchConditionWithId(sc.getSearchConditionId());
                    if (persisted != null) {
                        sc.setCreatedDisplayValue(persisted.getCreatedDisplayValue());
                        sc.setModifiedDisplayValue(persisted.getModifiedDisplayValue());
                    }
                }
            }
        }
    }

    protected SearchCondition fetchSearchConditionWithId(final Long scId) {
        return getDsl().selectFrom(SEARCH_CONDITION).where(SEARCH_CONDITION.SEARCH_CONDITION_ID.eq(scId)).fetchOneInto(SearchCondition.class);
    }

    /*
     * Taking care of searchConditions that do not have searchTerms
     */
    private void addSearchTermLessConditionsOf(SearchOrder searchOrder) {
        if (searchOrder != null && searchOrder.getId() != null) {
            final Long searchOrderId = searchOrder.getId();
            final List<Long> conditionIdsWithSearchTerms = findConditionIdsWithSearchTerms(searchOrderId);
            final List<SearchCondition> termLessConditions = findTermLessConditions(searchOrderId, conditionIdsWithSearchTerms);
            for (final SearchCondition sc : termLessConditions) {
                searchOrder.add(sc);
            }
        }
    }

    protected List<Long> findConditionIdsWithSearchTerms(final Long searchOrderId) {
        final List<Long> conditionIdsWithSearchTerms = getDsl().select(SEARCH_TERM.SEARCH_CONDITION_ID)
                .from(SEARCH_TERM)
                .innerJoin(SEARCH_CONDITION)
                .on(SEARCH_TERM.SEARCH_CONDITION_ID.eq(SEARCH_CONDITION.SEARCH_CONDITION_ID))
                .where(SEARCH_CONDITION.SEARCH_ORDER_ID.eq(searchOrderId))
                .fetchInto(Long.class);
        return conditionIdsWithSearchTerms;
    }

    protected List<SearchCondition> findTermLessConditions(final Long searchOrderId, final List<Long> conditionIdsWithSearchTerms) {
        final List<SearchCondition> termLessConditions = getDsl().selectFrom(SEARCH_CONDITION)
                .where(SEARCH_CONDITION.SEARCH_ORDER_ID.eq(searchOrderId))
                .and(SEARCH_CONDITION.SEARCH_CONDITION_ID.notIn(conditionIdsWithSearchTerms))
                .fetchInto(SearchCondition.class);
        return termLessConditions;
    }

    private void fillExcludedPaperIdsInto(SearchOrder searchOrder) {
        final List<Long> excludedPaperIds = fetchExcludedPaperIdsForSearchOrderWithId(searchOrder.getId());
        searchOrder.setExcludedPaperIds(excludedPaperIds);
    }

    protected List<Long> fetchExcludedPaperIdsForSearchOrderWithId(final long searchOrderId) {
        // @formatter:off
        return getDsl()
                .select(SEARCH_EXCLUSION.PAPER_ID)
                .from(SEARCH_EXCLUSION)
                .where(SEARCH_EXCLUSION.SEARCH_ORDER_ID.equal(searchOrderId))
                .fetch(r -> (Long) r.get(0));
        // @formatter:on
    }

    private void fillCodesIntoSearchConditionsOf(SearchOrder searchOrder) {
        for (SearchCondition sc : searchOrder.getSearchConditions()) {
            fillCodesInto(sc);
        }
    }

    @Override
    protected void updateAssociatedEntities(final SearchOrder searchOrder) {
        storeSearchConditionsOf(searchOrder);
        storeExcludedIdsOf(searchOrder);
    }

    @Override
    protected void saveAssociatedEntitiesOf(final SearchOrder searchOrder) {
        storeSearchConditionsOf(searchOrder);
        storeExcludedIdsOf(searchOrder);
    }

    private void storeSearchConditionsOf(SearchOrder searchOrder) {
        storeExistingConditionsOf(searchOrder);
        deleteObsoleteConditionsFrom(searchOrder);
    }

    private void storeExistingConditionsOf(SearchOrder searchOrder) {
        final Long searchOrderId = searchOrder.getId();
        for (final SearchCondition sc : searchOrder.getSearchConditions()) {
            Long searchConditionId = sc.getSearchConditionId();
            if (searchConditionId == null) {
                addSearchCondition(sc, searchOrderId);
            } else {
                updateSearchCondition(sc, searchOrderId);
            }
        }
    }

    private void updateSearchTerm(final SearchTerm<?> st, final Long searchTermId, final Long searchConditionId) {
        final Condition idMatches = SEARCH_TERM.ID.eq(searchTermId);
        getDsl().update(SEARCH_TERM)
                .set(row(SEARCH_TERM.SEARCH_CONDITION_ID, SEARCH_TERM.SEARCH_TERM_TYPE, SEARCH_TERM.FIELD_NAME, SEARCH_TERM.RAW_VALUE, SEARCH_TERM.LAST_MODIFIED, SEARCH_TERM.LAST_MODIFIED_BY,
                        SEARCH_TERM.VERSION),
                        row(searchConditionId, st.getSearchTermType().getId(), st.getFieldName(), st.getRawSearchTerm(), getTs(), getUserId(),
                                getDsl().select(SEARCH_TERM.VERSION).from(SEARCH_TERM).where(idMatches).fetchOneInto(Integer.class) + 1))
                .where(idMatches)
                .execute();
    }

    private void deleteObsoleteConditionsFrom(SearchOrder searchOrder) {
        final List<Long> conditionIds = searchOrder.getSearchConditions().stream().map(SearchCondition::getSearchConditionId).collect(Collectors.toList());
        getDsl().deleteFrom(SEARCH_CONDITION).where(SEARCH_CONDITION.SEARCH_ORDER_ID.equal(searchOrder.getId()).and(SEARCH_CONDITION.SEARCH_CONDITION_ID.notIn(conditionIds))).execute();
        for (final SearchCondition sc : searchOrder.getSearchConditions()) {
            removeObsoleteSearchTerms(sc, sc.getSearchConditionId());
        }
    }

    private void storeExcludedIdsOf(SearchOrder searchOrder) {
        storeExistingExclusionsOf(searchOrder);
        deleteObsoleteExclusionsOf(searchOrder);
    }

    private void storeExistingExclusionsOf(SearchOrder searchOrder) {
        final long searchOrderId = searchOrder.getId();
        final List<Long> saved = getDsl().select(SEARCH_EXCLUSION.PAPER_ID)
                .from(SEARCH_EXCLUSION)
                .where(SEARCH_EXCLUSION.SEARCH_ORDER_ID.eq(searchOrderId))
                .and(SEARCH_EXCLUSION.PAPER_ID.in(searchOrder.getExcludedPaperIds()))
                .fetchInto(Long.class);
        final List<Long> unsaved = new ArrayList<>(searchOrder.getExcludedPaperIds());
        unsaved.removeAll(saved);
        final Integer userId = getUserId();
        for (final Long excludedId : unsaved) {
            getDsl().insertInto(SEARCH_EXCLUSION, SEARCH_EXCLUSION.SEARCH_ORDER_ID, SEARCH_EXCLUSION.PAPER_ID, SEARCH_EXCLUSION.CREATED_BY, SEARCH_EXCLUSION.LAST_MODIFIED_BY)
                    .values(searchOrderId, excludedId, userId, userId)
                    .execute();
        }
    }

    private void deleteObsoleteExclusionsOf(SearchOrder searchOrder) {
        getDsl().deleteFrom(SEARCH_EXCLUSION).where(SEARCH_EXCLUSION.SEARCH_ORDER_ID.eq(searchOrder.getId())).and(SEARCH_EXCLUSION.PAPER_ID.notIn(searchOrder.getExcludedPaperIds())).execute();
    }

    /** {@inheritDoc} */
    @Override
    public SearchCondition addSearchCondition(SearchCondition searchCondition, long searchOrderId) {
        final Optional<SearchCondition> optionalPersisted = findEquivalentPersisted(searchCondition, searchOrderId);
        if (optionalPersisted.isPresent()) {
            return optionalPersisted.get();
        } else {
            final Integer userId = getUserId();
            final SearchConditionRecord searchConditionRecord = getDsl()
                    .insertInto(SEARCH_CONDITION, SEARCH_CONDITION.SEARCH_ORDER_ID, SEARCH_CONDITION.CREATED_TERM, SEARCH_CONDITION.MODIFIED_TERM, SEARCH_CONDITION.CREATED_BY,
                            SEARCH_CONDITION.LAST_MODIFIED_BY)
                    .values(searchOrderId, searchCondition.getCreatedDisplayValue(), searchCondition.getModifiedDisplayValue(), userId, userId)
                    .returning()
                    .fetchOne();
            persistSearchTerms(searchCondition, searchConditionRecord.getSearchConditionId());
            persistCodes(searchCondition, searchConditionRecord.getSearchConditionId());
            final SearchCondition persistedSearchCondition = getDsl().selectFrom(SEARCH_CONDITION)
                    .where(SEARCH_CONDITION.SEARCH_CONDITION_ID.eq(searchConditionRecord.getSearchConditionId()))
                    .fetchOneInto(SearchCondition.class);
            fillSearchTermsInto(persistedSearchCondition, mapSearchTermsToSearchConditions(persistedSearchCondition));
            fillCodesInto(persistedSearchCondition);
            return persistedSearchCondition;
        }
    }

    /**
     * Tries to load an already persisted instance of {@link SearchCondition} for the given search order (identified by the 
     * <code>searchOrderId</code>) semantically covering the same searchConditions.
     * @param searchCondition the search condition we're trying to find the semantically identical persisted version for.
     * @param searchOrderId identifying the search order
     * @return optional of the persisted version (if found - empty othewise)
     */
    private Optional<SearchCondition> findEquivalentPersisted(final SearchCondition searchCondition, final long searchOrderId) {
        final List<SearchCondition> persisted = getDsl().selectFrom(SEARCH_CONDITION).where(SEARCH_CONDITION.SEARCH_ORDER_ID.eq(searchOrderId)).fetchInto(SearchCondition.class);
        for (final SearchCondition sc : persisted) {
            Long searchConditionId = sc.getSearchConditionId();
            fillSearchTermsInto(sc, mapSearchTermsToSearchConditions(sc));
            fillCodesInto(sc);
            sc.setSearchConditionId(null);
            if (searchCondition.equals(sc)) {
                sc.setSearchConditionId(searchConditionId);
                return Optional.ofNullable(sc);
            }
        }
        return Optional.empty();
    }

    private void persistSearchTerms(SearchCondition searchCondition, Long searchConditionId) {
        saveOrUpdateValidTerms(searchCondition, searchConditionId);
        removeObsoleteSearchTerms(searchCondition, searchConditionId);
    }

    private void fillCodesInto(SearchCondition searchCondition) {
        final List<Code> codes = fetchCodesForSearchConditionWithId(searchCondition);
        if (CollectionUtils.isNotEmpty(codes)) {
            searchCondition.addCodes(codes);
        }
    }

    protected List<Code> fetchCodesForSearchConditionWithId(final SearchCondition searchCondition) {
        final String localizationCode = getLocalization().getLocalization();
        final List<Code> codes = getDsl()
        // @formatter:off
            .select(CODE.CODE_.as("C_ID")
                    , DSL.coalesce(CODE_TR.NAME, TranslationUtils.NOT_TRANSL).as("C_NAME")
                    , CODE_TR.COMMENT.as("C_COMMENT")
                    , CODE.INTERNAL.as("C_INTERNAL")
                    , CODE_CLASS.ID.as("CC_ID")
                    , DSL.coalesce(CODE_CLASS_TR.NAME, TranslationUtils.NOT_TRANSL).as("CC_NAME")
                    , DSL.coalesce(CODE_CLASS_TR.DESCRIPTION, TranslationUtils.NOT_TRANSL).as("CC_DESCRIPTION")
                    , CODE.SORT)
            .from(SEARCH_CONDITION_CODE)
            .join(SEARCH_CONDITION).on(SEARCH_CONDITION_CODE.SEARCH_CONDITION_ID.equal(SEARCH_CONDITION.SEARCH_CONDITION_ID))
            .join(CODE).on(SEARCH_CONDITION_CODE.CODE.equal(CODE.CODE_))
            .join(CODE_CLASS).on(CODE.CODE_CLASS_ID.equal(CODE_CLASS.ID))
            .leftOuterJoin(CODE_TR).on(CODE.CODE_.equal(CODE_TR.CODE).and(CODE_TR.LANG_CODE.equal(localizationCode)))
            .leftOuterJoin(CODE_CLASS_TR).on(CODE_CLASS.ID.equal(CODE_CLASS_TR.CODE_CLASS_ID).and(CODE_CLASS_TR.LANG_CODE.equal(localizationCode)))
            .where(SEARCH_CONDITION_CODE.SEARCH_CONDITION_ID.equal(searchCondition.getSearchConditionId()))
            .fetchInto(Code.class);
        // @formatter:on
        return codes;
    }

    private void saveOrUpdateValidTerms(SearchCondition searchCondition, Long searchConditionId) {
        InsertValuesStep6<SearchTermRecord, Long, Integer, String, String, Integer, Integer> insertStep = getDsl().insertInto(SEARCH_TERM, SEARCH_TERM.SEARCH_CONDITION_ID,
                SEARCH_TERM.SEARCH_TERM_TYPE, SEARCH_TERM.FIELD_NAME, SEARCH_TERM.RAW_VALUE, SEARCH_TERM.CREATED_BY, SEARCH_TERM.LAST_MODIFIED_BY);
        final Integer userId = getUserId();
        boolean hasPendingInsert = false;
        for (final BooleanSearchTerm bst : searchCondition.getBooleanSearchTerms()) {
            final int typeId = bst.getSearchTermType().getId();
            final String fieldName = bst.getFieldName();
            final BooleanSearchTerm pbst = (BooleanSearchTerm) getPersistedTerm(searchConditionId, fieldName, BooleanSearchTerm.class, typeId);
            if (pbst != null) {
                updateSearchTerm(bst, pbst.getId(), searchConditionId);
            } else {
                insertStep = insertStep.values(searchConditionId, typeId, fieldName, bst.getRawSearchTerm(), userId, userId);
                hasPendingInsert = true;
            }
        }
        for (final IntegerSearchTerm ist : searchCondition.getIntegerSearchTerms()) {
            final int typeId = ist.getSearchTermType().getId();
            final String fieldName = ist.getFieldName();
            final IntegerSearchTerm pist = (IntegerSearchTerm) getPersistedTerm(searchConditionId, fieldName, BooleanSearchTerm.class, typeId);
            if (pist != null) {
                updateSearchTerm(ist, pist.getId(), searchConditionId);
            } else {
                insertStep = insertStep.values(searchConditionId, typeId, fieldName, ist.getRawSearchTerm(), userId, userId);
                hasPendingInsert = true;
            }
        }
        for (final StringSearchTerm sst : searchCondition.getStringSearchTerms()) {
            final int typeId = sst.getSearchTermType().getId();
            final String fieldName = sst.getFieldName();
            final StringSearchTerm pist = (StringSearchTerm) getPersistedTerm(searchConditionId, fieldName, BooleanSearchTerm.class, typeId);
            if (pist != null) {
                updateSearchTerm(sst, pist.getId(), searchConditionId);
            } else {
                insertStep = insertStep.values(searchConditionId, typeId, fieldName, sst.getRawSearchTerm(), userId, userId);
                hasPendingInsert = true;
            }
        }
        if (hasPendingInsert)
            insertStep.execute();
    }

    private SearchTerm<?> getPersistedTerm(final Long searchConditionId, final String fieldName, final Class<BooleanSearchTerm> termClass, final int typeId) {
        return getDsl().select(SEARCH_TERM.ID, SEARCH_TERM.SEARCH_CONDITION_ID, SEARCH_TERM.FIELD_NAME, SEARCH_TERM.RAW_VALUE)
                .from(SEARCH_TERM)
                .where(SEARCH_TERM.SEARCH_CONDITION_ID.eq(searchConditionId))
                .and(SEARCH_TERM.SEARCH_TERM_TYPE.eq(typeId))
                .and(SEARCH_TERM.FIELD_NAME.eq(fieldName))
                .fetchOneInto(termClass);
    }

    private void removeObsoleteSearchTerms(SearchCondition searchCondition, Long searchConditionId) {
        if (!searchCondition.getRemovedKeys().isEmpty()) {
            getDsl().deleteFrom(SEARCH_TERM).where(SEARCH_TERM.SEARCH_CONDITION_ID.eq(searchConditionId)).and(SEARCH_TERM.FIELD_NAME.in(searchCondition.getRemovedKeys())).execute();
            searchCondition.clearRemovedKeys();
        }
    }

    private void persistCodes(SearchCondition searchCondition, Long searchConditionId) {
        saveOrUpdateCodes(searchCondition, searchConditionId);
        removeObsoleteCodesFrom(searchCondition, searchConditionId);
    }

    private void saveOrUpdateCodes(SearchCondition searchCondition, Long searchConditionId) {
        if (!CollectionUtils.isEmpty(searchCondition.getCodes())) {
            InsertValuesStep4<SearchConditionCodeRecord, Long, String, Integer, Integer> step = getDsl().insertInto(SEARCH_CONDITION_CODE, SEARCH_CONDITION_CODE.SEARCH_CONDITION_ID,
                    SEARCH_CONDITION_CODE.CODE, SEARCH_CONDITION_CODE.CREATED_BY, SEARCH_CONDITION_CODE.LAST_MODIFIED_BY);
            final Integer userId = getUserId();
            for (final Code c : searchCondition.getCodes()) {
                step = step.values(searchConditionId, c.getCode(), userId, userId);
            }
            step.onDuplicateKeyIgnore().execute();
        }
    }

    private void removeObsoleteCodesFrom(SearchCondition searchCondition, Long searchConditionId) {
        final List<String> codes = searchCondition.getCodes().stream().map(Code::getCode).collect(Collectors.toList());
        getDsl().deleteFrom(SEARCH_CONDITION_CODE).where(SEARCH_CONDITION_CODE.SEARCH_CONDITION_ID.equal(searchConditionId).and(SEARCH_CONDITION_CODE.CODE.notIn(codes))).execute();
    }

    /** {@inheritDoc} */
    @Override
    public SearchCondition updateSearchCondition(SearchCondition searchCondition, long searchOrderId) {
        final Condition idMatches = SEARCH_CONDITION.SEARCH_CONDITION_ID.eq(searchCondition.getSearchConditionId());
        getDsl().update(SEARCH_CONDITION)
                .set(row(SEARCH_CONDITION.SEARCH_ORDER_ID, SEARCH_CONDITION.CREATED_TERM, SEARCH_CONDITION.MODIFIED_TERM, SEARCH_CONDITION.LAST_MODIFIED, SEARCH_CONDITION.LAST_MODIFIED_BY,
                        SEARCH_CONDITION.VERSION),
                        row(searchOrderId, searchCondition.getCreatedDisplayValue(), searchCondition.getModifiedDisplayValue(), getTs(), getUserId(),
                                getDsl().select(SEARCH_CONDITION.VERSION).from(SEARCH_CONDITION).where(idMatches).fetchOneInto(Integer.class) + 1))
                .where(idMatches)
                .execute();
        persistSearchTerms(searchCondition, searchCondition.getSearchConditionId());
        persistCodes(searchCondition, searchCondition.getSearchConditionId());
        SearchCondition persistedSearchCondition = fetchSearchConditionWithId(searchCondition.getSearchConditionId());
        fillSearchTermsInto(persistedSearchCondition, mapSearchTermsToSearchConditions(persistedSearchCondition));
        fillCodesInto(persistedSearchCondition);
        return persistedSearchCondition;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteSearchConditionWithId(long searchConditionId) {
        getDsl().deleteFrom(SEARCH_CONDITION).where(SEARCH_CONDITION.SEARCH_CONDITION_ID.eq(searchConditionId)).execute();
    }

}
