package dev.gideonwhite1029.horizon.commands;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandArgument {
    private final List<CommandArgumentType<?>> argumentTypes;
    private final List<List<String>> tabComplete;

    public CommandArgument(CommandArgumentType<?>... argumentTypes) {
        this.argumentTypes = List.of(argumentTypes);
        this.tabComplete = new ArrayList<>();
        for (int i = 0; i < argumentTypes.length; i++) {
            tabComplete.add(new ArrayList<>());
        }
    }

    public List<String> tabComplete(int n) {
        if (tabComplete.size() > n) {
            return tabComplete.get(n);
        } else {
            return List.of();
        }
    }

    public CommandArgument setTabComplete(int index, List<String> list) {
        tabComplete.set(index, list);
        return this;
    }

    public CommandArgumentResult parse(int index, String @NotNull [] args) {
        Object[] result = new Object[argumentTypes.size()];
        Arrays.fill(result, null);
        for (int i = index, j = 0; i < args.length && j < result.length; i++, j++) {
            result[j] = argumentTypes.get(j).pasre(args[i]);
        }
        return new CommandArgumentResult(new ArrayList<>(Arrays.asList(result)));
    }
}
