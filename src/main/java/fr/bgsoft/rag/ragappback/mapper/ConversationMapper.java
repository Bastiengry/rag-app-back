package fr.bgsoft.rag.ragappback.mapper;

import org.mapstruct.Mapper;

import fr.bgsoft.rag.ragappback.dto.LightConversationDto;
import fr.bgsoft.rag.ragappback.dto.ConversationMessageDto;
import fr.bgsoft.rag.ragappback.entity.Conversation;
import fr.bgsoft.rag.ragappback.entity.ConversationMessage;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    
    LightConversationDto toLightDto(Conversation conversation);
    
    ConversationMessageDto toMessageDto(ConversationMessage message);
}