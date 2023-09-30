package com.aston.bankingservice;

import com.aston.bankingservice.model.AccountDto;
import com.aston.bankingservice.model.AccountNewDto;
import com.aston.bankingservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity.status(CREATED).body(accountService.create(accountNewDto));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDto> getAccount(@RequestParam int accountNumber) {

    }

    @GetMapping("/accounts/all")
    public ResponseEntity<List<AccountDto>> getAllAccounts() {

    }

    @PatchMapping("/accounts/deposit/{accountNumber}")
    public ResponseEntity<AccountDto> depositMoney(@RequestParam int accountNumber) {

    }

    @PatchMapping("/accounts/withdraw/{accountNumber}")
    public ResponseEntity<AccountDto> withdrawMoney(@RequestParam int accountNumber,
                                                    @RequestHeader("X-pin-code") String pin) {

    }

    @PatchMapping("/accounts/transfer/{accountNumber}")
    public ResponseEntity<AccountDto> transferMoney(@RequestParam int accountNumber,
                                                    @RequestHeader("X-pin-code") String pin) {

    }
}
