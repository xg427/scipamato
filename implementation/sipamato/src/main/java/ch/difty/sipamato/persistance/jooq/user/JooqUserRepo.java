package ch.difty.sipamato.persistance.jooq.user;

import static ch.difty.sipamato.db.tables.SipamatoUser.SIPAMATO_USER;
import static ch.difty.sipamato.db.tables.UserRole.USER_ROLE;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.difty.sipamato.auth.Role;
import ch.difty.sipamato.db.tables.records.SipamatoUserRecord;
import ch.difty.sipamato.db.tables.records.UserRoleRecord;
import ch.difty.sipamato.entity.User;
import ch.difty.sipamato.lib.DateTimeService;
import ch.difty.sipamato.persistance.jooq.GenericFilterConditionMapper;
import ch.difty.sipamato.persistance.jooq.InsertSetStepSetter;
import ch.difty.sipamato.persistance.jooq.JooqEntityRepo;
import ch.difty.sipamato.persistance.jooq.JooqSortMapper;
import ch.difty.sipamato.persistance.jooq.UpdateSetStepSetter;
import ch.difty.sipamato.service.Localization;

/**
 * The repository to manage {@link User}s - including the nested list of {@link Roles}s.
 *
 * @author u.joss
 */
@Repository
public class JooqUserRepo extends JooqEntityRepo<SipamatoUserRecord, User, Integer, ch.difty.sipamato.db.tables.SipamatoUser, UserRecordMapper, UserFilter> implements UserRepository {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqUserRepo.class);

    @Autowired
    public JooqUserRepo(DSLContext dsl, UserRecordMapper mapper, JooqSortMapper<SipamatoUserRecord, User, ch.difty.sipamato.db.tables.SipamatoUser> sortMapper,
            GenericFilterConditionMapper<UserFilter> filterConditionMapper, DateTimeService dateTimeService, Localization localization,
            InsertSetStepSetter<SipamatoUserRecord, User> insertSetStepSetter, UpdateSetStepSetter<SipamatoUserRecord, User> updateSetStepSetter) {
        super(dsl, mapper, sortMapper, filterConditionMapper, dateTimeService, localization, insertSetStepSetter, updateSetStepSetter);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected Class<? extends User> getEntityClass() {
        return User.class;
    }

    @Override
    protected Class<? extends SipamatoUserRecord> getRecordClass() {
        return SipamatoUserRecord.class;
    }

    @Override
    protected ch.difty.sipamato.db.tables.SipamatoUser getTable() {
        return SIPAMATO_USER;
    }

    @Override
    protected TableField<SipamatoUserRecord, Integer> getTableId() {
        return SIPAMATO_USER.ID;
    }

    @Override
    protected Integer getIdFrom(SipamatoUserRecord record) {
        return record.getId();
    }

    @Override
    protected Integer getIdFrom(User entity) {
        return entity.getId();
    }

    @Override
    protected void enrichAssociatedEntitiesOf(User entity) {
        if (entity != null) {
            final List<Role> roles = getDsl().select(USER_ROLE.ROLE_ID).from(USER_ROLE).where(USER_ROLE.USER_ID.eq(entity.getId())).fetch().map(rec -> Role.of((Integer) rec.getValue(0)));
            if (CollectionUtils.isNotEmpty(roles)) {
                entity.setRoles(roles);
            }
        }
    }

    @Override
    protected void saveAssociatedEntitiesOf(User user) {
        storeNewRolesOf(user);
    }

    @Override
    protected void updateAssociatedEntities(User user) {
        storeNewRolesOf(user);
        deleteObsoleteRolesFrom(user);
    }

    private void storeNewRolesOf(User user) {
        InsertValuesStep2<UserRoleRecord, Integer, Integer> step = getDsl().insertInto(USER_ROLE, USER_ROLE.USER_ID, USER_ROLE.ROLE_ID);
        final Integer userId = user.getId();
        for (final Role r : user.getRoles()) {
            step = step.values(userId, r.getId());
        }
        step.onDuplicateKeyIgnore().execute();
    }

    private void deleteObsoleteRolesFrom(User user) {
        final List<Integer> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
        getDsl().deleteFrom(USER_ROLE).where(USER_ROLE.USER_ID.equal(user.getId()).and(USER_ROLE.ROLE_ID.notIn(roleIds))).execute();
    }

    /** {@inheritDoc} */
    @Override
    public User findByUserName(String userName) {
        final List<User> users = getDsl().selectFrom(SIPAMATO_USER).where(SIPAMATO_USER.USER_NAME.eq(userName)).fetchInto(User.class);
        if (users.isEmpty()) {
            return null;
        } else {
            User user = users.get(0);
            enrichAssociatedEntitiesOf(user);
            return user;
        }
    }

}
