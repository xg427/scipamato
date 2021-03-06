package ch.difty.scipamato.core.persistence.paper;

import static ch.difty.scipamato.core.db.tables.Code.CODE;
import static ch.difty.scipamato.core.db.tables.CodeClass.CODE_CLASS;
import static ch.difty.scipamato.core.db.tables.CodeClassTr.CODE_CLASS_TR;
import static ch.difty.scipamato.core.db.tables.CodeTr.CODE_TR;
import static ch.difty.scipamato.core.db.tables.Newsletter.NEWSLETTER;
import static ch.difty.scipamato.core.db.tables.NewsletterTopic.NEWSLETTER_TOPIC;
import static ch.difty.scipamato.core.db.tables.NewsletterTopicTr.NEWSLETTER_TOPIC_TR;
import static ch.difty.scipamato.core.db.tables.Paper.PAPER;
import static ch.difty.scipamato.core.db.tables.PaperAttachment.PAPER_ATTACHMENT;
import static ch.difty.scipamato.core.db.tables.PaperCode.PAPER_CODE;
import static ch.difty.scipamato.core.db.tables.PaperNewsletter.PAPER_NEWSLETTER;
import static ch.difty.scipamato.core.db.tables.SearchExclusion.SEARCH_EXCLUSION;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep4;
import org.jooq.Record6;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import ch.difty.scipamato.common.AssertAs;
import ch.difty.scipamato.common.DateTimeService;
import ch.difty.scipamato.common.TranslationUtils;
import ch.difty.scipamato.common.config.ApplicationProperties;
import ch.difty.scipamato.common.persistence.GenericFilterConditionMapper;
import ch.difty.scipamato.common.persistence.JooqSortMapper;
import ch.difty.scipamato.common.persistence.paging.PaginationContext;
import ch.difty.scipamato.core.db.tables.records.PaperCodeRecord;
import ch.difty.scipamato.core.db.tables.records.PaperRecord;
import ch.difty.scipamato.core.entity.Code;
import ch.difty.scipamato.core.entity.Paper;
import ch.difty.scipamato.core.entity.PaperAttachment;
import ch.difty.scipamato.core.entity.search.PaperFilter;
import ch.difty.scipamato.core.entity.search.SearchOrder;
import ch.difty.scipamato.core.persistence.InsertSetStepSetter;
import ch.difty.scipamato.core.persistence.JooqEntityRepo;
import ch.difty.scipamato.core.persistence.UpdateSetStepSetter;
import ch.difty.scipamato.core.persistence.paper.searchorder.PaperBackedSearchOrderRepository;

/**
 * The repository to manage {@link Paper}s - including the nested list of
 * {@link Code}s.
 *
 * @author u.joss
 */
@Repository
@Slf4j
public class JooqPaperRepo extends
    JooqEntityRepo<PaperRecord, Paper, Long, ch.difty.scipamato.core.db.tables.Paper, PaperRecordMapper, PaperFilter>
    implements PaperRepository {

    private static final String LANGUAGE_CODE = "languageCode";

    private final PaperBackedSearchOrderRepository searchOrderRepository;

    public JooqPaperRepo(@Qualifier("dslContext") final DSLContext dsl, final PaperRecordMapper mapper,
        final JooqSortMapper<PaperRecord, Paper, ch.difty.scipamato.core.db.tables.Paper> sortMapper,
        final GenericFilterConditionMapper<PaperFilter> filterConditionMapper, final DateTimeService dateTimeService,
        final InsertSetStepSetter<PaperRecord, Paper> insertSetStepSetter,
        final UpdateSetStepSetter<PaperRecord, Paper> updateSetStepSetter,
        final PaperBackedSearchOrderRepository searchOrderRepository,
        final ApplicationProperties applicationProperties) {
        super(dsl, mapper, sortMapper, filterConditionMapper, dateTimeService, insertSetStepSetter, updateSetStepSetter,
            applicationProperties);
        this.searchOrderRepository = AssertAs.notNull(searchOrderRepository, "searchOrderRepository");
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected ch.difty.scipamato.core.db.tables.Paper getTable() {
        return PAPER;
    }

    @Override
    protected TableField<PaperRecord, Long> getTableId() {
        return PAPER.ID;
    }

    @Override
    protected TableField<PaperRecord, Integer> getRecordVersion() {
        return PAPER.VERSION;
    }

    @Override
    protected Long getIdFrom(final PaperRecord record) {
        return record.getId();
    }

    @Override
    protected Long getIdFrom(final Paper entity) {
        return entity.getId();
    }

    @Override
    protected void enrichAssociatedEntitiesOf(final Paper entity, final String languageCode) {
        if (entity != null) {
            if (languageCode != null) {
                enrichCodesOf(entity, languageCode);
                enrichNewsletterAssocation(entity, languageCode);
            }
            enrichAttachmentsOf(entity);
        }
    }

    private void enrichCodesOf(final Paper entity, final String languageCode) {
        final List<Code> codes = getDsl()
            .select(CODE.CODE_.as("C_ID"), DSL
                    .coalesce(CODE_TR.NAME, TranslationUtils.NOT_TRANSL)
                    .as("C_NAME"), CODE_TR.COMMENT.as("C_COMMENT"), CODE.INTERNAL.as("C_INTERNAL"),
                CODE_CLASS.ID.as("CC_ID"), DSL
                    .coalesce(CODE_CLASS_TR.NAME, TranslationUtils.NOT_TRANSL)
                    .as("CC_NAME"), DSL
                    .coalesce(CODE_CLASS_TR.DESCRIPTION, TranslationUtils.NOT_TRANSL)
                    .as("CC_DESCRIPTION"), CODE.SORT, CODE.CREATED, CODE.CREATED_BY, CODE.LAST_MODIFIED,
                CODE.LAST_MODIFIED_BY, CODE.VERSION)
            .from(PAPER_CODE)
            .join(PAPER)
            .on(PAPER_CODE.PAPER_ID.equal(PAPER.ID))
            .join(CODE)
            .on(PAPER_CODE.CODE.equal(CODE.CODE_))
            .join(CODE_CLASS)
            .on(CODE.CODE_CLASS_ID.equal(CODE_CLASS.ID))
            .leftOuterJoin(CODE_TR)
            .on(CODE.CODE_
                .equal(CODE_TR.CODE)
                .and(CODE_TR.LANG_CODE.equal(languageCode)))
            .leftOuterJoin(CODE_CLASS_TR)
            .on(CODE_CLASS.ID
                .equal(CODE_CLASS_TR.CODE_CLASS_ID)
                .and(CODE_CLASS_TR.LANG_CODE.equal(languageCode)))
            .where(PAPER_CODE.PAPER_ID.equal(entity.getId()))
            .fetchInto(Code.class);
        if (CollectionUtils.isNotEmpty(codes))
            entity.addCodes(codes);
    }

    private void enrichNewsletterAssocation(final Paper entity, final String languageCode) {
        Record6<Integer, String, Integer, Integer, String, String> r = getDsl()
            .select(NEWSLETTER.ID, NEWSLETTER.ISSUE, NEWSLETTER.PUBLICATION_STATUS,
                PAPER_NEWSLETTER.NEWSLETTER_TOPIC_ID, NEWSLETTER_TOPIC_TR.TITLE, PAPER_NEWSLETTER.HEADLINE)
            .from(NEWSLETTER)
            .innerJoin(PAPER_NEWSLETTER)
            .on(NEWSLETTER.ID.eq(PAPER_NEWSLETTER.NEWSLETTER_ID))
            .leftOuterJoin(NEWSLETTER_TOPIC)
            .on(PAPER_NEWSLETTER.NEWSLETTER_TOPIC_ID.eq(NEWSLETTER_TOPIC.ID))
            .leftOuterJoin(NEWSLETTER_TOPIC_TR)
            .on(NEWSLETTER_TOPIC.ID.eq(NEWSLETTER_TOPIC_TR.NEWSLETTER_TOPIC_ID))
            .where(PAPER_NEWSLETTER.PAPER_ID
                .eq(entity.getId())
                .and(PAPER_NEWSLETTER.NEWSLETTER_TOPIC_ID
                    .isNull()
                    .or(NEWSLETTER_TOPIC_TR.LANG_CODE.eq(languageCode))))
            .fetchOne();
        if (r != null)
            entity.setNewsletterLink(r.value1(), r.value2(), r.value3(), r.value4(), r.value5(), r.value6());
    }

    private void enrichAttachmentsOf(final Paper entity) {
        final Long id = entity.getId();
        if (id != null)
            entity.setAttachments(loadSlimAttachment(id));
    }

    public List<ch.difty.scipamato.core.entity.PaperAttachment> loadSlimAttachment(final long paperId) {
        return getDsl()
            .select(PAPER_ATTACHMENT.ID, PAPER_ATTACHMENT.PAPER_ID, PAPER_ATTACHMENT.NAME,
                PAPER_ATTACHMENT.CONTENT_TYPE, PAPER_ATTACHMENT.SIZE, PAPER_ATTACHMENT.CREATED_BY,
                PAPER_ATTACHMENT.CREATED, PAPER_ATTACHMENT.LAST_MODIFIED_BY, PAPER_ATTACHMENT.LAST_MODIFIED,
                PAPER_ATTACHMENT.VERSION)
            .from(PAPER_ATTACHMENT)
            .where(PAPER_ATTACHMENT.PAPER_ID.eq(paperId))
            .orderBy(PAPER_ATTACHMENT.ID)
            .fetchInto(ch.difty.scipamato.core.entity.PaperAttachment.class);
    }

    @Override
    protected void saveAssociatedEntitiesOf(final Paper paper, final String languageCode) {
        storeNewCodesOf(paper);
    }

    @Override
    protected void updateAssociatedEntities(final Paper paper, final String languageCode) {
        storeNewCodesOf(paper);
        deleteObsoleteCodesFrom(paper);
        considerStoringNewsletterLinkOf(paper);
    }

    private void storeNewCodesOf(final Paper paper) {
        InsertValuesStep4<PaperCodeRecord, Long, String, Integer, Integer> step = getDsl().insertInto(PAPER_CODE,
            PAPER_CODE.PAPER_ID, PAPER_CODE.CODE, PAPER_CODE.CREATED_BY, PAPER_CODE.LAST_MODIFIED_BY);
        final Long paperId = paper.getId();
        final Integer userId = getUserId();
        for (final Code c : paper.getCodes())
            step = step.values(paperId, c.getCode(), userId, userId);
        step
            .onDuplicateKeyIgnore()
            .execute();
    }

    private void deleteObsoleteCodesFrom(final Paper paper) {
        final List<String> codes = paper
            .getCodes()
            .stream()
            .map(Code::getCode)
            .collect(Collectors.toList());
        getDsl()
            .deleteFrom(PAPER_CODE)
            .where(PAPER_CODE.PAPER_ID
                .equal(paper.getId())
                .and(PAPER_CODE.CODE.notIn(codes)))
            .execute();
    }

    /**
     * Insert or update the association between paper and newsletter.
     *
     * @param paper
     */
    private void considerStoringNewsletterLinkOf(final Paper paper) {
        if (paper != null && paper.getNewsletterLink() != null) {
            final Paper.NewsletterLink nl = paper.getNewsletterLink();
            getDsl()
                .insertInto(PAPER_NEWSLETTER)
                .columns(PAPER_NEWSLETTER.NEWSLETTER_ID, PAPER_NEWSLETTER.PAPER_ID,
                    PAPER_NEWSLETTER.NEWSLETTER_TOPIC_ID, PAPER_NEWSLETTER.HEADLINE, PAPER_NEWSLETTER.CREATED,
                    PAPER_NEWSLETTER.CREATED_BY)
                .values(nl.getNewsletterId(), paper.getId(), nl.getTopicId(), nl.getHeadline(), getTs(), getUserId())
                .onDuplicateKeyUpdate()
                .set(PAPER_NEWSLETTER.NEWSLETTER_TOPIC_ID, nl.getTopicId())
                .set(PAPER_NEWSLETTER.HEADLINE, nl.getHeadline())
                .set(PAPER_NEWSLETTER.LAST_MODIFIED, getTs())
                .set(PAPER_NEWSLETTER.LAST_MODIFIED_BY, getUserId())
                .where(PAPER_NEWSLETTER.NEWSLETTER_ID
                    .eq(nl.getNewsletterId())
                    .and(PAPER_NEWSLETTER.PAPER_ID.eq(paper.getId())))
                .execute();
        }
    }

    @Override
    public List<Paper> findByIds(final List<Long> ids) {
        AssertAs.notNull(ids, "ids");
        return getDsl()
            .selectFrom(PAPER)
            .where(PAPER.ID.in(ids))
            .fetch(getMapper());
    }

    @Override
    public List<Paper> findWithCodesByIds(final List<Long> ids, final String languageCode) {
        AssertAs.notNull(languageCode, LANGUAGE_CODE);
        final List<Paper> papers = findByIds(ids);
        enrichAssociatedEntitiesOfAll(papers, languageCode);
        return papers;
    }

    @Override
    public List<Paper> findBySearchOrder(final SearchOrder searchOrder, final String languageCode) {
        AssertAs.notNull(languageCode, LANGUAGE_CODE);
        final List<Paper> papers = searchOrderRepository.findBySearchOrder(searchOrder);
        enrichAssociatedEntitiesOfAll(papers, languageCode);
        return papers;
    }

    @Override
    public List<Paper> findPageBySearchOrder(final SearchOrder searchOrder, final PaginationContext paginationContext,
        final String languageCode) {
        AssertAs.notNull(languageCode, LANGUAGE_CODE);
        final List<Paper> entities = searchOrderRepository.findPageBySearchOrder(searchOrder, paginationContext);
        enrichAssociatedEntitiesOfAll(entities, languageCode);
        return entities;
    }

    @Override
    public int countBySearchOrder(final SearchOrder searchOrder) {
        return searchOrderRepository.countBySearchOrder(searchOrder);
    }

    @Override
    public List<Paper> findByPmIds(final List<Integer> pmIds, final String languageCode) {
        if (CollectionUtils.isEmpty(pmIds)) {
            return new ArrayList<>();
        } else {
            AssertAs.notNull(languageCode, LANGUAGE_CODE);
            final List<Paper> papers = getDsl()
                .selectFrom(PAPER)
                .where(PAPER.PM_ID.in(pmIds))
                .fetch(getMapper());
            enrichAssociatedEntitiesOfAll(papers, languageCode);
            return papers;
        }
    }

    @Override
    public List<Integer> findExistingPmIdsOutOf(final List<Integer> pmIds) {
        if (CollectionUtils.isEmpty(pmIds)) {
            return new ArrayList<>();
        } else {
            return getDsl()
                .select(PAPER.PM_ID)
                .from(PAPER)
                .where(PAPER.PM_ID.in(pmIds))
                .fetchInto(Integer.class);
        }
    }

    @Override
    public List<Paper> findByNumbers(final List<Long> numbers, final String languageCode) {
        if (CollectionUtils.isEmpty(numbers)) {
            return new ArrayList<>();
        } else {
            AssertAs.notNull(languageCode, LANGUAGE_CODE);
            final List<Paper> papers = getDsl()
                .selectFrom(PAPER)
                .where(PAPER.NUMBER.in(numbers))
                .fetch(getMapper());
            enrichAssociatedEntitiesOfAll(papers, languageCode);
            return papers;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: If {@code minimumPaperNumberToBeRecycled} is itself part of a gap
     * range, the current implementation will not return any numbers of the same gap
     * range.
     * <p>
     * Example:
     * <ul>
     * <li>assume the first gap to be in the range [5,9], the second gap is
     * {@code 17}.</li>
     * <li>the last used value is {@code 36}
     * <li>assume {@code minimumPaperNumberToBeRecycled=6}</li>
     * <li>calling the method twice will return the following values: {@code 17},
     * {@code 37}</li>
     * <li>it will ignore the values {@code 6} to {@code 9} which are in the same
     * gap as the minimum value</li>
     * </ul>
     **/
    @Override
    public long findLowestFreeNumberStartingFrom(final long minimumPaperNumberToBeRecycled) {
        final ch.difty.scipamato.core.db.tables.Paper p = PAPER.as("p");
        final ch.difty.scipamato.core.db.tables.Paper pn = PAPER.as("pn");

        final Long freeNumber = getDsl()
            .select(p.NUMBER.plus(1L))
            .from(p)
            .leftOuterJoin(pn)
            .on(pn.NUMBER.eq(p.NUMBER.plus(1L)))
            .where(pn.NUMBER
                .isNull()
                .and(p.NUMBER.ge(minimumPaperNumberToBeRecycled)))
            .limit(1)
            .fetchOneInto(Long.class);
        return freeNumber != null ? freeNumber : minimumPaperNumberToBeRecycled;
    }

    @Override
    public List<Long> findPageOfIdsBySearchOrder(final SearchOrder searchOrder,
        final PaginationContext paginationContext) {
        return searchOrderRepository.findPageOfIdsBySearchOrder(searchOrder, paginationContext);
    }

    @Override
    public void excludePaperFromSearchOrderResults(final long searchOrderId, final long paperId) {
        getDsl()
            .insertInto(SEARCH_EXCLUSION)
            .columns(SEARCH_EXCLUSION.SEARCH_ORDER_ID, SEARCH_EXCLUSION.PAPER_ID)
            .values(searchOrderId, paperId)
            .onConflictDoNothing()
            .execute();
    }

    @Override
    public void reincludePaperIntoSearchOrderResults(final long searchOrderId, final long paperId) {
        getDsl()
            .deleteFrom(SEARCH_EXCLUSION)
            .where(SEARCH_EXCLUSION.SEARCH_ORDER_ID
                .eq(searchOrderId)
                .and(SEARCH_EXCLUSION.PAPER_ID.eq(paperId)))
            .execute();
    }

    @Override
    public Paper saveAttachment(final ch.difty.scipamato.core.entity.PaperAttachment pa) {
        getDsl()
            .insertInto(PAPER_ATTACHMENT)
            .columns(PAPER_ATTACHMENT.PAPER_ID, PAPER_ATTACHMENT.NAME, PAPER_ATTACHMENT.CONTENT,
                PAPER_ATTACHMENT.CONTENT_TYPE, PAPER_ATTACHMENT.SIZE, PAPER_ATTACHMENT.CREATED,
                PAPER_ATTACHMENT.CREATED_BY, PAPER_ATTACHMENT.LAST_MODIFIED, PAPER_ATTACHMENT.LAST_MODIFIED_BY,
                PAPER_ATTACHMENT.VERSION)
            .values(pa.getPaperId(), pa.getName(), pa.getContent(), pa.getContentType(), pa.getSize(),
                getDateTimeService().getCurrentTimestamp(), getUserId(), getDateTimeService().getCurrentTimestamp(),
                getUserId(), 1)
            .onConflict(PAPER_ATTACHMENT.PAPER_ID, PAPER_ATTACHMENT.NAME)
            .doUpdate()
            .set(PAPER_ATTACHMENT.CONTENT, pa.getContent())
            .set(PAPER_ATTACHMENT.CONTENT_TYPE, pa.getContentType())
            .set(PAPER_ATTACHMENT.SIZE, pa.getSize())
            .set(PAPER_ATTACHMENT.LAST_MODIFIED, getDateTimeService().getCurrentTimestamp())
            .set(PAPER_ATTACHMENT.LAST_MODIFIED_BY, getUserId())
            .set(PAPER_ATTACHMENT.VERSION, PAPER_ATTACHMENT.VERSION.plus(1))
            .execute();
        getLogger().info("Saved attachment '{}' for paper with id {}.", pa.getName(), pa.getPaperId());
        return findById(pa.getPaperId());
    }

    @Override
    public PaperAttachment loadAttachmentWithContentBy(final Integer id) {
        return getDsl()
            .select(PAPER_ATTACHMENT.ID, PAPER_ATTACHMENT.PAPER_ID, PAPER_ATTACHMENT.NAME, PAPER_ATTACHMENT.CONTENT,
                PAPER_ATTACHMENT.CONTENT_TYPE, PAPER_ATTACHMENT.SIZE, PAPER_ATTACHMENT.CREATED_BY,
                PAPER_ATTACHMENT.CREATED, PAPER_ATTACHMENT.LAST_MODIFIED_BY, PAPER_ATTACHMENT.LAST_MODIFIED,
                PAPER_ATTACHMENT.VERSION)
            .from(PAPER_ATTACHMENT)
            .where(PAPER_ATTACHMENT.ID.eq(id))
            .fetchOneInto(ch.difty.scipamato.core.entity.PaperAttachment.class);
    }

    @Override
    public Paper deleteAttachment(final Integer id) {
        if (id != null) {
            final Long paperId = getDsl()
                .select(PAPER_ATTACHMENT.PAPER_ID)
                .from(PAPER_ATTACHMENT)
                .where(PAPER_ATTACHMENT.ID.eq(id))
                .fetchOneInto(Long.class);
            getDsl()
                .deleteFrom(PAPER_ATTACHMENT)
                .where(PAPER_ATTACHMENT.ID.eq(id))
                .execute();
            getLogger().info("Deleted attachment with id {}.", id);
            return findById(paperId);
        }
        return null;
    }

    @Override
    public void delete(final List<Long> ids) {
        getDsl()
            .deleteFrom(PAPER)
            .where(PAPER.ID.in(ids))
            .execute();
    }

}
