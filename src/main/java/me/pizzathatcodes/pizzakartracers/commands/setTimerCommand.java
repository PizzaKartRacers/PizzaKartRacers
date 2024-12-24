package me.pizzathatcodes.pizzakartracers.commands;

import me.pizzathatcodes.pizzakartracers.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

public class setTimerCommand extends Command {
    public setTimerCommand() {
        super("settimer");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /setTimer <time>");
        });

        var numberArgument = ArgumentType.Integer("time");

        numberArgument.setCallback((sender, exception) -> {
            sender.sendMessage("Invalid number: " + exception.getInput());
        });

        addSyntax((sender, context) -> {
            if(Main.getQueue() == null) {
                sender.sendMessage("Queue is no longer available");
                return;
            }
            int time = context.get(numberArgument);
            sender.sendMessage("Set timer to " + time + " seconds");
            Main.getQueue().setTimeWaitLeft(time);
        }, numberArgument);
    }
}
