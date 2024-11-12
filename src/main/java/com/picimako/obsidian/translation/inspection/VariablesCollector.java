package com.picimako.obsidian.translation.inspection;

import com.intellij.util.containers.SmartHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Collects the {@code {{...}}}-style variables from translation values.
 */
public final class VariablesCollector {
    /**
     * Non-greedy because of ?, so that multiple variables can be annotated separately in the same string.
     */
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{.+?}}");

    static Set<String> collectVariablesIn(@NotNull String value) {
        //SmartHashSet to optimize for 0 or 1 variable per entry
        var variables = new SmartHashSet<String>();
        var matcher = VARIABLE_PATTERN.matcher(value);

        while (matcher.find())
            //Trimming because there may be a variable specified as '{{ device }}'
            variables.add(value.substring(matcher.start() + 2, matcher.end() - 2).trim());

        return variables;
    }

    private VariablesCollector() {
        //Utility class
    }
}
