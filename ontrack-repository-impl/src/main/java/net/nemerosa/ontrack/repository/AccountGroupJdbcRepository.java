package net.nemerosa.ontrack.repository;

import net.nemerosa.ontrack.model.security.AccountGroup;
import net.nemerosa.ontrack.repository.support.AbstractJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class AccountGroupJdbcRepository extends AbstractJdbcRepository implements AccountGroupRepository {

    @Autowired
    public AccountGroupJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Collection<AccountGroup> findByAccount(int accountId) {
        return getNamedParameterJdbcTemplate().query(
                "SELECT G.* FROM ACCOUNT_GROUPS G " +
                        "INNER JOIN ACCOUNT_GROUP_LINK L ON L.ACCOUNTGROUP = G.ID " +
                        "WHERE L.ACCOUNT = :accountId",
                params("accountId", accountId),
                (rs, num) -> toAccountGroup(rs)
        );
    }

    private AccountGroup toAccountGroup(ResultSet rs) throws SQLException {
        return AccountGroup.of(
                rs.getString("name"),
                rs.getString("description")
        ).withId(id(rs));
    }

    @Override
    public List<AccountGroup> findAll() {
        return getJdbcTemplate().query(
                "SELECT * FORM ACCOUNT_GROUPS ORDER BY NAME",
                (rs, num) -> toAccountGroup(rs)
        );
    }
}
