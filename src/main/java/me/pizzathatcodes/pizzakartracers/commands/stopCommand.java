package me.pizzathatcodes.pizzakartracers.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public final class stopCommand extends Command implements CommandExecutor {

    public stopCommand() {
        super("stop", "exit", "quit", "end", "shutdown", "halt", "terminate");
        setCondition(this::condition);
        setDefaultExecutor(this);
    }

    private boolean condition(final CommandSender sender, final String command) {
        return sender.isConsole();
    }

    @Override
    public void apply(@NotNull final CommandSender sender, @NotNull final CommandContext context) {
        MinecraftServer.stopCleanly();
    }

}
