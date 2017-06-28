package ch.difty.scipamato.persistance.jooq.paper.searchorder;

import static ch.difty.scipamato.db.tables.Paper.PAPER;
import static ch.difty.scipamato.db.tables.ScipamatoUser.SCIPAMATO_USER;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import ch.difty.scipamato.entity.Paper;
import ch.difty.scipamato.entity.filter.AuditSearchTerm;
import ch.difty.scipamato.entity.filter.AuditSearchTerm.MatchType;
import ch.difty.scipamato.entity.filter.AuditSearchTerm.Token;
import ch.difty.scipamato.lib.AssertAs;
import ch.difty.scipamato.persistance.jooq.ConditionalSupplier;

/**
 * {@link SearchTermEvaluator} implementation evaluating audit searchTerms.
 *
 * @author u.joss
 */
class AuditSearchTermEvaluator implements SearchTermEvaluator<AuditSearchTerm> {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** {@inheritDoc} */
    @Override
    public Condition evaluate(final AuditSearchTerm searchTerm) {
        AssertAs.notNull(searchTerm, "searchTerm");

        final ConditionalSupplier conditions = new ConditionalSupplier();

        for (final Token token : searchTerm.getTokens()) {
            if (token.getType().matchType == MatchType.CONTAINS) {
                handleUserField(searchTerm, conditions, token);
            } else if (token.getType().matchType != MatchType.NONE) {
                handleDateField(searchTerm, conditions, token);
            }
        }
        return conditions.combineWithAnd();
    }

    private void handleUserField(final AuditSearchTerm searchTerm, final ConditionalSupplier conditions, final Token token) {
        final String fieldName = searchTerm.getFieldName();
        if (!(Paper.CREATED_BY.equals(fieldName) || Paper.LAST_MOD_BY.equals(fieldName))) {
            checkFields(fieldName, "user", Paper.CREATED_BY, Paper.LAST_MOD_BY, "CONTAINS");
        }
        final Field<Object> field = DSL.field(fieldName);
        final String userName = "%" + token.getUserSqlData().toLowerCase() + "%";
        final SelectConditionStep<Record1<Long>> step = DSL.select(PAPER.ID)
                .from(PAPER)
                .innerJoin(SCIPAMATO_USER)
                .on(field.eq(SCIPAMATO_USER.ID))
                .where(SCIPAMATO_USER.USER_NAME.lower().like(userName));
        conditions.add(() -> PAPER.ID.in(step));
    }

    private void checkFields(final String fieldName, String fieldType, String fld1, String fld2, String matchType) {
        final String msg = String.format("Field %s is not one of the expected %s fields [%s, %s] entitled to use MatchType.%s", fieldName, fieldType, fld1, fld2, matchType);
        throw new IllegalArgumentException(msg);
    }

    private void handleDateField(final AuditSearchTerm searchTerm, final ConditionalSupplier conditions, final Token token) {
        final String fieldName = searchTerm.getFieldName();
        if (!(Paper.CREATED.equals(fieldName) || Paper.LAST_MOD.equals(fieldName))) {
            checkFields(fieldName, "date", Paper.CREATED, Paper.LAST_MOD, token.getType().matchType.name());
        }
        final LocalDateTime ldt = LocalDateTime.parse(token.getDateSqlData(), DateTimeFormatter.ofPattern(DATE_FORMAT));
        addToConditions(token, DSL.field(fieldName), DSL.val(Timestamp.valueOf(ldt)), conditions);
    }

    private void addToConditions(final Token token, final Field<Object> field, final Field<Timestamp> value, final ConditionalSupplier conditions) {
        switch (token.getType().matchType) {
        case GREATER_THAN:
            conditions.add(() -> field.greaterThan(value));
            break;
        case GREATER_OR_EQUAL:
            conditions.add(() -> field.greaterOrEqual(value));
            break;
        case EQUALS:
            conditions.add(() -> field.equal(value));
            break;
        case LESS_OR_EQUAL:
            conditions.add(() -> field.lessOrEqual(value));
            break;
        case LESS_THAN:
            conditions.add(() -> field.lessThan(value));
            break;
        case CONTAINS:
        case NONE:
            break;
        default:
            throw new UnsupportedOperationException("Evaluation of type " + token.getType().matchType + " is not supported...");
        }
    }
}