package dev.gideonwhite1029.horizon.commands;

import org.jetbrains.annotations.NotNull;

public abstract class CommandArgumentType<E> {

    public static final CommandArgumentType<Integer> INTEGER = new CommandArgumentType<>() {
        @Override
        public Integer pasre(@NotNull String arg) {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };

    public static final CommandArgumentType<Double> DOUBLE = new CommandArgumentType<>() {
        @Override
        public Double pasre(@NotNull String arg) {
            try {
                return Double.parseDouble(arg);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };

    public static final CommandArgumentType<String> STRING = new CommandArgumentType<>() {
        @Override
        public String pasre(@NotNull String arg) {
            return arg;
        }
    };

    public abstract E pasre(@NotNull String arg);
}
