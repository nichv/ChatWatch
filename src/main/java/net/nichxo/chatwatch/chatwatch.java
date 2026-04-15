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

@Mod(modid = "chatwatch", name = "ChatWatch", version = "26.4.15", clientSideOnly = true)
public class chatwatch {

    private final Minecraft mc = Minecraft.getMinecraft();

    // ---- Strafing ----
    private boolean isStrafing = false;
    private boolean strafeLeft = false;
    private long strafeEndTime = 0;
    private long nextStrafeTime = System.currentTimeMillis() + 50000;

    private long pauseStrafeUntil = 0;

    // ---- Simple control flag ----
    private boolean chatWatchActive = false;
    private boolean wasChatOpen = false;

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
        boolean isChatOpen = mc.currentScreen instanceof GuiChat;

        // --------------------
        // PRESS P → ENABLE MODE
        // --------------------
        if (chatKey.isPressed()) {
            chatWatchActive = true;
            mc.displayGuiScreen(new GuiChat());

            pauseStrafeUntil = currentTime + 43000 + (long)(Math.random() * 8000);
        }

        // --------------------
        // CHAT CLOSED
        // --------------------
        if (wasChatOpen && !isChatOpen) {

            if (chatWatchActive) {

                // ESC pressed → fully disable
                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    chatWatchActive = false;
                    resetStrafe();
                } else {
                    // Otherwise assume ENTER → reopen chat
                    mc.displayGuiScreen(new GuiChat());
                }
            }
        }

        wasChatOpen = isChatOpen;

        // --------------------
        // STRICT CONTROL WHEN ACTIVE
        // --------------------
        if (chatWatchActive) {

            // If ANY GUI other than chat is open → block movement
            if (!(mc.currentScreen instanceof GuiChat)) {
                stopMovement();
                return;
            }

            // --------------------
            // STRAFING ONLY WHEN CHAT OPEN
            // --------------------
            if (currentTime < pauseStrafeUntil) {
                stopMovement();
                return;
            }

            if (!isStrafing && currentTime >= nextStrafeTime) {
                isStrafing = true;

                strafeLeft = Math.random() < 0.5;
                long duration = 444 + (long)(Math.random() * 900);
                strafeEndTime = currentTime + duration;
            }

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

			// 1 minute delay (+ small randomness)
			nextStrafeTime = currentTime + 50000 + (long)(Math.random() * 5000);

			stopMovement();
			}
			}

        } else {
            // System OFF → normal controls
            restoreMovement();
        }
    }

    // --------------------
    // HELPERS
    // --------------------
    private void stopMovement() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
    }

    private void restoreMovement() {

    // If ANY GUI is open, block movement completely
    if (mc.currentScreen != null) {
        stopMovement();
        return;
    }

    // Otherwise allow normal movement
    KeyBinding.setKeyBindState(
            mc.gameSettings.keyBindLeft.getKeyCode(),
            Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())
    );

    KeyBinding.setKeyBindState(
            mc.gameSettings.keyBindRight.getKeyCode(),
            Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())
    );
}

    private void resetStrafe() {
        isStrafing = false;
        stopMovement();
    }
}