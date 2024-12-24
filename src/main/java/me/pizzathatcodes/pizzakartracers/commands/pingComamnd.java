package me.pizzathatcodes.pizzakartracers.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class pingComamnd extends Command {
    public pingComamnd() {
        super("ping");

        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            player.sendMessage(player.getLatency() + "ms");
        });
    }
}
