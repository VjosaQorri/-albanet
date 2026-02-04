package com.example.albanet.ticket.internal;

import com.example.albanet.ticket.api.dto.TicketDetailsResponse;
import com.example.albanet.ticket.api.dto.TicketResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class TicketMapper {

    public TicketResponse toResponse(TicketEntity entity) {
        if (entity == null) {
            return null;
        }

        TicketResponse response = new TicketResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setPriority(entity.getPriority() != null ? entity.getPriority().name() : null);
        response.setAssignedTeam(entity.getAssignedTeam());
        response.setAssignedTo(stringToLong(entity.getAssignedTo()));
        response.setCreatedAt(toOffset(entity.getCreatedAt()));
        response.setUpdatedAt(toOffset(entity.getUpdatedAt()));

        return response;
    }

    public TicketDetailsResponse toDetailsResponse(TicketEntity entity) {
        if (entity == null) {
            return null;
        }

        TicketDetailsResponse response = new TicketDetailsResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setCategory(entity.getCategory());
        response.setProblemType(entity.getProblemType());
        response.setStatus(entity.getStatus());
        response.setPriority(entity.getPriority());
        response.setAssignedTeam(entity.getAssignedTeam());
        response.setAssignedTo(entity.getAssignedTo());
        response.setCustomerId(entity.getCustomerId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        response.setClosedAt(entity.getClosedAt());
        response.setResolutionSummary(entity.getResolutionSummary());

        return response;
    }

    private Long stringToLong(String s) {
        if (s == null) {
            return null;
        }
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private OffsetDateTime toOffset(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        return dt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
