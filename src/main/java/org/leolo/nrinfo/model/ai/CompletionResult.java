package org.leolo.nrinfo.model.ai;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class CompletionResult {
    private final List<String> choices;
    private final long timeTaken;
    private final int totalTokens;
    private final int inputTokens;
    private final int outputTokens;
    private final Instant createdTime;

    public CompletionResult(List<String> result, long timeTaken, int totalTokens, int inputTokens, int outputTokens) {
        this.choices = List.copyOf(result);
        this.timeTaken = timeTaken;
        this.totalTokens = totalTokens;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        createdTime = Instant.now();
    }

    public CompletionResult(String result, long timeTaken, int totalTokens, int inputTokens, int outputTokens) {
        this(List.of(result), timeTaken, totalTokens, inputTokens, outputTokens);
    }
}
