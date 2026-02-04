package com.example.albanet.chat.internal;

import com.example.albanet.chat.api.dto.ChatDto;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatDto toDto(ChatEntity entity) {
        if (entity == null) {
            return null;
        }

        ChatDto dto = new ChatDto();
        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());

        return dto;
    }
}
