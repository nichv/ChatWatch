package net.nichxo.chatwatch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import org.lwjgl.input.Keyboard;

@Mod(modid = "chatwatch", name = "ChatWatch", version = "1.1", clientSideOnly = true)
public class chatwatch {

    private final Minecraft mc = Minecraft.getMinecraft();

    // ---- Strafing ----
    private boolean isStrafing = false;
    private boolean strafeLeft = false;
    private long strafeEndTime = 0;
    private long nextStrafeTime = System.currentTimeMillis() + 40000;

    // ---- Chat reopen ----
    private boolean shouldReopenChat = false;
    private long reopenTime = 0;

    // ---- Human timing ----
    private long pauseStrafeUntil = 0;

    private static KeyBinding chatKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        chatKey = new KeyBinding("Chat + Human Strafe", Keyboard.KEY_P, "ChatWatch");
        ClientRegistry.registerKeyBinding(chatKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {

        long currentTime = System.currentTimeMillis();

        // --------------------
        // OPEN CHAT
        // --------------------
        if (chatKey.isPressed()) {
            mc.displayGuiScreen(new GuiChat());

            // small human reaction delay before movement
            pauseStrafeUntil = currentTime + (100 + (int)(Math.random() * 150));
        }

        // --------------------
        // DETECT MESSAGE SEND
        // --------------------
        if (mc.currentScreen instanceof GuiChat && Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {

            shouldReopenChat = true;

            // human delay before reopening
            reopenTime = currentTime + (120 + (int)(Math.random() * 140));

            // pause strafing after sending (thinking moment)
            pauseStrafeUntil = currentTime + (300 + (int)(Math.random() * 400));

            // reset next strafe (avoid instant movement)
            nextStrafeTime = currentTime + (20000 + (long)(Math.random() * 30000));
        }

        // --------------------
        // REOPEN CHAT (DELAYED)
        // --------------------
        if (!(mc.currentScreen instanceof GuiChat)
                && shouldReopenChat
                && currentTime >= reopenTime) {

            mc.displayGuiScreen(new GuiChat());
            shouldReopenChat = false;
        }

        // --------------------
        // HUMAN-LIKE STRAFING
        // --------------------
        if (mc.currentScreen instanceof GuiChat) {

            // don't move during "thinking"
            if (currentTime < pauseStrafeUntil) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                return;
            }

            // start new strafe randomly
            if (!isStrafing && currentTime >= nextStrafeTime) {
                isStrafing = true;

                strafeLeft = Math.random() < 0.5;

                // uneven duration (human inconsistency)
                long duration = 200 + (long)(Math.random() * 900);
                strafeEndTime = currentTime + duration;
            }

            // active strafe
            if (isStrafing) {

                KeyBinding.setKeyBindState(
                        mc.gameSettings.keyBindLeft.getKeyCode(),
                        strafeLeft
                );

                KeyBinding.setKeyBindState(
                        mc.gameSettings.keyBindRight.getKeyCode(),
                        !strafeLeft
                );

                if (currentTime >= strafeEndTime) {
                    isStrafing = false;

                    // hesitation before next move
                    nextStrafeTime = currentTime + (long)((2000 + (Math.random() * 8000)) * 0.85);

                    KeyBinding.setKeyBindState(
                            mc.gameSettings.keyBindLeft.getKeyCode(), false
                    );

                    KeyBinding.setKeyBindState(
                            mc.gameSettings.keyBindRight.getKeyCode(), false
                    );
                }
            }

        } else {
            // reset keys outside chat
            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindLeft.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())
            );

            KeyBinding.setKeyBindState(
                    mc.gameSettings.keyBindRight.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())
            );
        }
    }
}