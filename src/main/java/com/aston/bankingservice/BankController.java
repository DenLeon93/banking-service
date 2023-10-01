package com.aston.bankingservice;

import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.service.AccountService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;


@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class BankController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountNewDto accountNewDto) {
        log.info("AccountController.createAccount invoke with accountNewDto={}", accountNewDto);
        return ResponseEntity.status(CREATED).body(accountService.create(accountNewDto));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable int accountNumber) {
        log.info("AccountController.getAccount invoke with accountNumber={}", accountNumber);
        return ResponseEntity.ok(accountService.getByNumber(accountNumber));
    }

    @GetMapping("/accounts/all")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {
        log.info("AccountController.getAllAccounts invoke");
        return ResponseEntity.ok(accountService.getAll());
    }

    @PatchMapping("/accounts/deposit/")
    public ResponseEntity<AccountDto> depositAction(@RequestParam String action,
                                                   @RequestParam @Positive float money,
                                                   @RequestHeader("X-user-account-number") int accountNumber,
                                                   @RequestHeader("X-user-pin-code") String pin) {
        log.info("AccountController.depositMoney invoke with accountNumber={}, action={}, money={}", accountNumber, action, money);
        return ResponseEntity.ok(accountService.depositAction(action, money,accountNumber, pin));
    }

    @PatchMapping("/accounts/transfer/{accountNumber}")
    public ResponseEntity<List<AccountDto>> transferMoney(@PathVariable int accountNumber,
                                                    @RequestParam @Positive float money,
                                                    @RequestHeader("X-user-account-number") int userAccountNumber,
                                                    @RequestHeader("X-user-pin-code") String pin) {
        log.info("AccountController.transferMoney invoke with userAccountNumber={}, money={}, accountNumber={},", userAccountNumber, money, accountNumber);

        return ResponseEntity.ok(accountService.transferMoney(accountNumber, money, userAccountNumber, pin));
    }
}
