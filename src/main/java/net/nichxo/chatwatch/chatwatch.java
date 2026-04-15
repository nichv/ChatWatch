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

@Mod(modid = "chatwatch", name = "ChatWatch", version = "26.4.15-b1", clientSideOnly = true)
public class chatwatch {

    private final Minecraft mc = Minecraft.getMinecraft();

    // ---- Strafing ----
    private boolean isStrafing = false;
    private boolean strafeLeft = false;
    private long strafeEndTime = 0;
    private long nextStrafeTime = System.currentTimeMillis() + 50000;

    private long pauseStrafeUntil = 0;

    // ---- Balance System ----
    private long strafeBalance = 0;
    private static final long MAX_BALANCE = 225;
    private static final long CUSHION = 50;
    private static final double OVERSHOOT = 1.12;

    // ---- Debug Delay Control ----
    public long minDelay = 50000;
    public long maxDelay = 55000;

    // ---- Home ----
    private double homeX, homeY, homeZ;

    // ---- Control ----
    private boolean chatWatchActive = false;
    private boolean wasChatOpen = false;

    private static KeyBinding chatKey;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        chatKey = new KeyBinding("Chat + Human Strafe", Keyboard.KEY_P, "ChatWatch");
        ClientRegistry.registerKeyBinding(chatKey);
        MinecraftForge.EVENT_BUS.register(this);

        // Register debug command
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CommandCWDelay(this));
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {

        long currentTime = System.currentTimeMillis();
        boolean isChatOpen = mc.currentScreen instanceof GuiChat;

        // --------------------
        // PRESS P → ENABLE MODE + STORE HOME
        // --------------------
        if (chatKey.isPressed()) {
            chatWatchActive = true;
            mc.displayGuiScreen(new GuiChat());

            homeX = mc.thePlayer.posX;
            homeY = mc.thePlayer.posY;
            homeZ = mc.thePlayer.posZ;

            strafeBalance = 0;

            pauseStrafeUntil = currentTime + 43000 + (long)(Math.random() * 8000);
        }

        // --------------------
        // CHAT CLOSED
        // --------------------
        if (wasChatOpen && !isChatOpen) {

            if (chatWatchActive) {

                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    chatWatchActive = false;
                    resetStrafe();
                } else {
                    mc.displayGuiScreen(new GuiChat());
                }
            }
        }

        wasChatOpen = isChatOpen;

        // --------------------
        // ACTIVE CONTROL
        // --------------------
        if (chatWatchActive) {

            if (!(mc.currentScreen instanceof GuiChat)) {
                stopMovement();
                return;
            }

            if (currentTime < pauseStrafeUntil) {
                stopMovement();
                return;
            }

            // --------------------
            // STRAFE LOGIC
            // --------------------
            if (!isStrafing && currentTime >= nextStrafeTime) {

                isStrafing = true;

                // Softer direction logic
                if (strafeBalance > (MAX_BALANCE - CUSHION)) {
                    strafeLeft = Math.random() < 0.8;
                } 
                else if (strafeBalance < -(MAX_BALANCE - CUSHION)) {
                    strafeLeft = Math.random() >= 0.8;
                } 
                else {
                    strafeLeft = Math.random() < 0.5;
                }

                // Max allowed with overshoot
                long maxAllowed;
                if (strafeLeft) {
                    maxAllowed = (long)((MAX_BALANCE + strafeBalance) * OVERSHOOT);
                } else {
                    maxAllowed = (long)((MAX_BALANCE - strafeBalance) * OVERSHOOT);
                }

                maxAllowed -= CUSHION;

                long duration = 120 + (long)(Math.random() * Math.min(700, Math.max(50, maxAllowed)));

                strafeEndTime = currentTime + duration;

                // Apply drift
                if (strafeLeft) {
                    strafeBalance -= duration;
                } else {
                    strafeBalance += duration;
                }
            }

            // Apply movement
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

                    // ✅ NEW: uses adjustable delay
                    long delay = minDelay + (long)(Math.random() * (maxDelay - minDelay));
                    nextStrafeTime = currentTime + delay;

                    stopMovement();
                }
            }

        } else {
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

        if (mc.currentScreen != null) {
            stopMovement();
            return;
        }

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
        strafeBalance = 0;
        stopMovement();
    }
}