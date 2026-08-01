package org.leolo.nrinfo.model.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class CompletionResult {
    private List<String> result;
    private long timeTaken;
    private int totalTokens;
    private int inputTokens;
    private int outputTokens;

    public CompletionResult(List<String> result, long timeTaken, int totalTokens, int inputTokens, int outputTokens) {
        this.result = result;
        this.timeTaken = timeTaken;
        this.totalTokens = totalTokens;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public CompletionResult(String result, long timeTaken, int totalTokens, int inputTokens, int outputTokens) {
        this.result = Collections.singletonList(result);
        this.timeTaken = timeTaken;
        this.totalTokens = totalTokens;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }
}
