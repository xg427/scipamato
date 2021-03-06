package ch.difty.scipamato.core.persistence;

import static ch.difty.scipamato.core.db.Tables.PAPER;
import static ch.difty.scipamato.core.persistence.TestDbConstants.MAX_ID_PREPOPULATED;
import static ch.difty.scipamato.core.persistence.TestDbConstants.RECORD_COUNT_PREPOPULATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class JooqDslTransactionIntegrationTest extends JooqBaseIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private DataSourceTransactionManager txMgr;

    @After
    public void teardown() {
        // Delete all books that were created in any test - just in case
        dsl
            .delete(PAPER)
            .where(PAPER.ID.gt(MAX_ID_PREPOPULATED))
            .execute();
    }

    @Test
    public void testExplicitTransactions() {
        boolean rollback = false;

        TransactionStatus tx = txMgr.getTransaction(new DefaultTransactionDefinition());
        try {
            // This is a "bug". The same book is created twice, resulting in a
            // constraint violation exception
            for (long i = 0; i < 2; i++)
                dsl
                    .insertInto(PAPER)
                    .set(PAPER.ID, MAX_ID_PREPOPULATED + 1)
                    .set(PAPER.NUMBER, 1000L)
                    .set(PAPER.AUTHORS, "authors")
                    .set(PAPER.FIRST_AUTHOR, "firstAuthor")
                    .set(PAPER.FIRST_AUTHOR_OVERRIDDEN, false)
                    .set(PAPER.TITLE, "title")
                    .set(PAPER.LOCATION, "location")
                    .set(PAPER.GOALS, "goals")
                    .execute();
            Assert.fail();
        } catch (DataAccessException e) {
            // Upon the constraint violation, we explicitly roll back the transaction.
            txMgr.rollback(tx);
            rollback = true;
        }

        assertEquals(RECORD_COUNT_PREPOPULATED, dsl.fetchCount(PAPER));
        assertTrue(rollback);
    }

    @Test
    public void testjOOQTransactionsSimple() {
        boolean rollback = false;

        try {
            dsl.transaction(c -> {

                // This is a "bug". The same book is created twice, resulting in a
                // constraint violation exception
                for (long i = 0; i < 2; i++)
                    dsl
                        .insertInto(PAPER)
                        .set(PAPER.ID, MAX_ID_PREPOPULATED + 1)
                        .set(PAPER.AUTHORS, "authors")
                        .set(PAPER.FIRST_AUTHOR, "firstAuthor")
                        .set(PAPER.FIRST_AUTHOR_OVERRIDDEN, false)
                        .set(PAPER.TITLE, "title")
                        .set(PAPER.LOCATION, "location")
                        .set(PAPER.GOALS, "goals")
                        .execute();
                Assert.fail();
            });
        } catch (DataAccessException e) {
            // Upon the constraint violation, the transaction must already have been rolled
            // back
            rollback = true;
        }

        assertEquals(RECORD_COUNT_PREPOPULATED, dsl.fetchCount(PAPER));
        assertTrue(rollback);
    }

    @Test
    public void testjOOQTransactionsNested() {
        AtomicBoolean rollback1 = new AtomicBoolean(false);
        AtomicBoolean rollback2 = new AtomicBoolean(false);

        try {
            // If using Spring transactions, we don't need the c1 reference
            dsl.transaction(c1 -> {

                // The first insertion will work
                dsl
                    .insertInto(PAPER)
                    .set(PAPER.ID, MAX_ID_PREPOPULATED + 1)
                    .set(PAPER.NUMBER, 1000L)
                    .set(PAPER.AUTHORS, "authors")
                    .set(PAPER.FIRST_AUTHOR, "firstAuthor")
                    .set(PAPER.FIRST_AUTHOR_OVERRIDDEN, false)
                    .set(PAPER.TITLE, "title2")
                    .set(PAPER.LOCATION, "location")
                    .set(PAPER.GOALS, "goals")
                    .execute();

                assertEquals(RECORD_COUNT_PREPOPULATED + 1, dsl.fetchCount(PAPER));

                try {
                    // Nest transactions using Spring. This should create a savepoint, right here
                    // If using Spring transactions, we don't need the c2 reference
                    dsl.transaction(c2 -> {

                        // The second insertion shouldn't work
                        for (long i = 0; i < 2; i++)
                            dsl
                                .insertInto(PAPER)
                                .set(PAPER.ID, 3L)
                                .set(PAPER.NUMBER, 1001L)
                                .set(PAPER.AUTHORS, "authors")
                                .set(PAPER.FIRST_AUTHOR, "firstAuthor")
                                .set(PAPER.FIRST_AUTHOR_OVERRIDDEN, false)
                                .set(PAPER.TITLE, "title3")
                                .set(PAPER.LOCATION, "location")
                                .set(PAPER.GOALS, "goals")
                                .execute();
                        Assert.fail();
                    });
                } catch (DataAccessException e) {
                    rollback1.set(true);
                }

                // We should've rolled back to the savepoint
                assertEquals(RECORD_COUNT_PREPOPULATED + 1, dsl.fetchCount(PAPER));

                throw new org.jooq.exception.DataAccessException("Rollback");
            });
        } catch (org.jooq.exception.DataAccessException e) {
            // Upon the constraint violation, the transaction must already have been rolled
            // back
            assertEquals("Rollback", e.getMessage());
            rollback2.set(true);
        }

        assertEquals(RECORD_COUNT_PREPOPULATED, dsl.fetchCount(PAPER));
        assertTrue(rollback2.get());
        assertTrue(rollback2.get());
    }

}
