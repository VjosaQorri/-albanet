package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository repository;
    private final TicketMapper mapper;

    public TicketServiceImpl(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TicketResponse create(CreateTicketRequest request, String actor) {
        // build entity deliberately (defaults, routing)
        // save
        // return mapper.toResponse(entity)
        throw new UnsupportedOperationException();
    }

    @Override
    public TicketResponse assign(AssignTicketRequest request, String actor) {
        // permission checks
        // assign logic
        throw new UnsupportedOperationException();
    }

    @Override
    public TicketResponse changePriority(ChangePriorityRequest request, String actor) {
        // manager/admin only
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TicketResponse> findAllForUser(String actor) {
        // filter by role/team
        throw new UnsupportedOperationException();
    }

    @Override
    public TicketDetailsResponse findDetails(Long ticketId, String actor) {
        // role-based visibility
        throw new UnsupportedOperationException();
    }
}
