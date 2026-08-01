package org.leolo.nrinfo.model.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Prompt {

    private String content;
    public Prompt(String content) {
        this.content = content;
    }
}
