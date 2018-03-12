package ru.chertenok.faqrecorder.telegraph;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

public interface TextPublisher {
    String getUrl(@NotNull List<String> text, @NotNull String Title, @Null String author);
}
