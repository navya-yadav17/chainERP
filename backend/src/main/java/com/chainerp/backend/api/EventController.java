package com.chainerp.backend.api;

import com.chainerp.backend.indexer.ContractEventRepository;
import com.chainerp.backend.indexer.ContractEventRepository.EventPage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/events/contracts")
public class EventController {
    private final ContractEventRepository repository;

    public EventController(ContractEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{contract}/events")
    public EventPage getContractEvents(
        @PathVariable String contract,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "page must be non-negative and size must be between 1 and 100"
            );
        }
        return repository.findEvents(contract, page, size);
    }
}
