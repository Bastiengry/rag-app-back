package fr.bgsoft.rag.ragappback.service.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import fr.bgsoft.rag.ragappback.config.ChatProperties;
import fr.bgsoft.rag.ragappback.dto.ChatResponseDto;
import fr.bgsoft.rag.ragappback.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final Deque<ChatMessage> history = new ArrayDeque<>();
    private final int historySize;

    public ChatServiceImpl(final ChatClient.Builder builder, final VectorStore vectorStore, final ChatProperties chatProperties) {
        this.chatClient = builder
            .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore,
                SearchRequest.defaults()
                    .withTopK(5)
                    .withSimilarityThreshold(0.0)))
            .build();
        this.historySize = chatProperties.getHistorySize();
    }

    @Override
    public Flux<ChatResponseDto> stream(String message) {
        List<ChatMessage> snapshot = getHistorySnapshot();
        addMessage(new ChatMessage(Role.USER, message));

        var prompt = chatClient.prompt()
            .system("Tu es un assistant expert. Utilise le contexte fourni pour répondre, mais si le contexte est vide, utilise tes connaissances générales.");

        for (ChatMessage previous : snapshot) {
            if (previous.role() == Role.USER) {
                prompt = prompt.user(previous.content());
            } else {
                prompt = prompt.system(previous.content());
            }
        }

        prompt = prompt.user(message);
        StringBuilder assistantBuilder = new StringBuilder();

        return prompt.stream()
            .content()
            .doOnNext(chunk -> {
                if (chunk != null) {
                    assistantBuilder.append(chunk);
                }
            })
            .map(chunk -> ChatResponseDto.builder()
                .message(chunk != null ? chunk : "")
                .build())
            .doOnComplete(() -> addMessage(new ChatMessage(Role.ASSISTANT, assistantBuilder.toString())));
    }

    private synchronized void addMessage(ChatMessage message) {
        history.addLast(message);
        while (history.size() > historySize) {
            history.removeFirst();
        }
    }

    private synchronized List<ChatMessage> getHistorySnapshot() {
        return new ArrayList<>(history);
    }

    private record ChatMessage(Role role, String content) {
    }

    private enum Role {
        USER,
        ASSISTANT
    }
}
