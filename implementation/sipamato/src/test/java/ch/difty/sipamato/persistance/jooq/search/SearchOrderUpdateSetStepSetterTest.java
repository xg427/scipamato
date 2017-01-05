package ch.difty.sipamato.persistance.jooq.search;

import static ch.difty.sipamato.db.tables.SearchOrder.SEARCH_ORDER;
import static ch.difty.sipamato.persistance.jooq.RecordMapperTest.CREATED;
import static ch.difty.sipamato.persistance.jooq.RecordMapperTest.CREATED_BY;
import static ch.difty.sipamato.persistance.jooq.RecordMapperTest.LAST_MOD;
import static ch.difty.sipamato.persistance.jooq.RecordMapperTest.LAST_MOD_BY;
import static ch.difty.sipamato.persistance.jooq.RecordMapperTest.VERSION;
import static ch.difty.sipamato.persistance.jooq.search.SearchOrderRecordMapperTest.GLOBAL;
import static ch.difty.sipamato.persistance.jooq.search.SearchOrderRecordMapperTest.ID;
import static ch.difty.sipamato.persistance.jooq.search.SearchOrderRecordMapperTest.NAME;
import static ch.difty.sipamato.persistance.jooq.search.SearchOrderRecordMapperTest.OWNER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import ch.difty.sipamato.db.tables.records.SearchOrderRecord;
import ch.difty.sipamato.entity.SearchOrder;
import ch.difty.sipamato.persistance.jooq.UpdateSetStepSetter;
import ch.difty.sipamato.persistance.jooq.UpdateSetStepSetterTest;

public class SearchOrderUpdateSetStepSetterTest extends UpdateSetStepSetterTest<SearchOrderRecord, SearchOrder> {

    private final UpdateSetStepSetter<SearchOrderRecord, SearchOrder> setter = new SearchOrderUpdateSetStepSetter();

    @Mock
    private SearchOrder entityMock;

    @Override
    protected UpdateSetStepSetter<SearchOrderRecord, SearchOrder> getSetter() {
        return setter;
    }

    @Override
    protected SearchOrder getEntity() {
        return entityMock;
    }

    @Override
    protected void specificTearDown() {
        verifyNoMoreInteractions(entityMock);
    }

    @Override
    protected void entityFixture() {
        when(entityMock.getId()).thenReturn(ID);
        SearchOrderRecordMapperTest.entityFixtureWithoutIdFields(entityMock);
    }

    @Override
    protected void stepSetFixtureExceptAudit() {
        when(getStep().set(SEARCH_ORDER.NAME, NAME)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.OWNER, OWNER)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.GLOBAL, GLOBAL)).thenReturn(getMoreStep());
    }

    @Override
    protected void stepSetFixtureAudit() {
        when(getMoreStep().set(SEARCH_ORDER.CREATED, CREATED)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.CREATED_BY, CREATED_BY)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.LAST_MODIFIED, LAST_MOD)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.LAST_MODIFIED_BY, LAST_MOD_BY)).thenReturn(getMoreStep());
        when(getMoreStep().set(SEARCH_ORDER.VERSION, VERSION + 1)).thenReturn(getMoreStep());
    }

    @Override
    protected void verifyCallToAllFieldsExceptAudit() {
        verify(entityMock).getName();
        verify(entityMock).getOwner();
        verify(entityMock).isGlobal();
    }

    @Override
    protected void verifyStepSettingExceptAudit() {
        verify(getStep()).set(SEARCH_ORDER.NAME, NAME);
        verify(getMoreStep()).set(SEARCH_ORDER.OWNER, OWNER);
        verify(getMoreStep()).set(SEARCH_ORDER.GLOBAL, GLOBAL);
    }

    @Override
    protected void verifyStepSettingAudit() {
        verify(getMoreStep()).set(SEARCH_ORDER.CREATED, CREATED);
        verify(getMoreStep()).set(SEARCH_ORDER.CREATED_BY, CREATED_BY);
        verify(getMoreStep()).set(SEARCH_ORDER.LAST_MODIFIED, LAST_MOD);
        verify(getMoreStep()).set(SEARCH_ORDER.LAST_MODIFIED_BY, LAST_MOD_BY);
        verify(getMoreStep()).set(SEARCH_ORDER.VERSION, VERSION + 1);
    }

}
