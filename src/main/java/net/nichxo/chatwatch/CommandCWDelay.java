package net.nichxo.chatwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandCWDelay extends CommandBase {

    private final chatwatch mod;

    public CommandCWDelay(chatwatch mod) {
        this.mod = mod;
    }

    @Override
    public String getCommandName() {
        return "cwdelay";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cwdelay <min_ms> <max_ms>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        if (args.length != 2) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new net.minecraft.util.ChatComponentText("Usage: /cwdelay <min> <max>")
            );
            return;
        }

        try {
            long min = Long.parseLong(args[0]);
            long max = Long.parseLong(args[1]);

            if (min < 0 || max < min) {
                throw new Exception();
            }

            mod.minDelay = min;
            mod.maxDelay = max;

            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    "ChatWatch delay set: " + min + " - " + max + " ms"
                )
            );

        } catch (Exception e) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new net.minecraft.util.ChatComponentText("Invalid numbers.")
            );
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // allow client use
    }
}