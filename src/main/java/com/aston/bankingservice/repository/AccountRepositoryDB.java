package com.aston.bankingservice.repository;

import com.aston.bankingservice.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class AccountRepositoryDB implements AccountRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override

    public Account save(Account account) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("accounts")
                .usingGeneratedKeyColumns("account_number");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", account.getName());
        parameters.put("pin", account.getPin());
        parameters.put("money", account.getMoney().floatValue());

        Number accountNumber = insert.executeAndReturnKey(parameters);
        account.setAccountNumber(accountNumber.intValue());
        log.info("AccountRepositoryDB.save insert account accountNumber={}",accountNumber.intValue());
        return account;
    }

    @Override
    public Optional<Account> findByNumber(int accountNumber) {
        String query = "SELECT * FROM accounts WHERE account_number = ?";
        List<Account> users = jdbcTemplate.query(query, this::makeAccount, accountNumber);
        if (users.isEmpty()) {
            log.info("Account with accountNumber={} not found", accountNumber);
            return Optional.empty();
        }
        log.info("Account found: accountNumber = {}", accountNumber);
        return Optional.of(users.get(0));
    }

    @Override
    public List<Account> getAll() {
        String query = "SELECT * FROM accounts";
        log.info("AccountRepositoryDB.getAll invoke");
        return jdbcTemplate.query(query, this::makeAccount);
    }

    @Override
    public Account update(Account account) {
        String sqlQuery = "UPDATE accounts SET name = ?, pin = ?, money = ? WHERE account_number = ?";
        jdbcTemplate.update(sqlQuery,
                account.getName(),
                account.getPin(),
                account.getMoney(),
                account.getAccountNumber());
        log.info("AccountRepositoryDB.update invoke");
        return account;
    }

    private Account makeAccount(ResultSet rs, int rowNum) throws SQLException {
        return Account.builder()
                .name(rs.getString("name"))
                .accountNumber(rs.getInt("account_number"))
                .pin(rs.getString("pin"))
                .money(BigDecimal.valueOf(rs.getFloat("money")))
                .build();
    }
}
