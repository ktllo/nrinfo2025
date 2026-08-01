package org.leolo.nrinfo.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ReasoningEffort;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.completions.CompletionUsage;
import org.leolo.nrinfo.model.ai.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIGenerationService {

    public static final String DEFAULT_MODEL = "gpt-5-nano";
    private final Logger logger = LoggerFactory.getLogger(AIGenerationService.class);

    @Autowired
    private ConfigurationService configurationService;

    private String getOpenAIAPIKey() {
        return configurationService.getString("openai.apikey");
    }

    public CompletionResult doCompletion(List<Prompt> prompts, String model, int maxTokens) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(getOpenAIAPIKey())
                .build();
        ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder()
                .model(model)
                .maxCompletionTokens(maxTokens);
        for (Prompt prompt : prompts) {
            if (prompt instanceof SystemPrompt) {
                params.addSystemMessage(prompt.getContent());
            } else if (prompt instanceof AssistantPrompt) {
                params.addAssistantMessage(prompt.getContent());
            } else {
                params.addUserMessage(prompt.getContent());
            }
        }
//        params.reasoningEffort(ReasoningEffort.LOW)
        long start = System.currentTimeMillis();
        ChatCompletion completion = client.chat().completions().create(params.build());
        long end = System.currentTimeMillis();

        if (completion.usage().isPresent()) {
            CompletionUsage usage = completion.usage().get();
            logger.info("Completion usage: Time taken: {}, Prompt: {}, Completion: {}, Total: {}", end - start, usage.promptTokens(), usage.completionTokens(), usage.totalTokens());

            return new CompletionResult(
                    completion.choices().getFirst().message().content().orElse(null),
                    end-start,
                    (int) usage.totalTokens(),
                    (int) usage.promptTokens(),
                    (int) usage.completionTokens()
            );
        }

        return new CompletionResult(
                completion.choices().getFirst().message().content().orElse(null),
                end-start,
                0,
                0,
                0
        );
    }

    public CompletionResult doCompletion(List<Prompt> prompts, int maxTokens) {
        return doCompletion(prompts, DEFAULT_MODEL, maxTokens);
    }
    public CompletionResult doCompletion(String prompt, String model, int maxTokens){
        return doCompletion(List.of(new UserPrompt(prompt)), model, maxTokens);
    }

    public CompletionResult doCompletion(String prompt){
        return doCompletion(prompt,DEFAULT_MODEL,1000);
    }

    public CompletionResult doCompletion(String prompt, int maxTokens){
        return doCompletion(prompt,DEFAULT_MODEL,maxTokens);
    }



}
