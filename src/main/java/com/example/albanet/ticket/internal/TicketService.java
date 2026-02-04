package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.dto.*;

import java.util.List;

public interface TicketService {

    TicketResponse create(CreateTicketRequest request, String actor);

    TicketResponse assign(AssignTicketRequest request, String actor);

    TicketResponse changePriority(ChangePriorityRequest request, String actor);

    List<TicketResponse> findAllForUser(String actor);

    TicketDetailsResponse findDetails(Long ticketId, String actor);
}
