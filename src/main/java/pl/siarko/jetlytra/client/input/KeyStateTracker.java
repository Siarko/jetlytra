package pl.siarko.jetlytra.client.input;

import net.minecraft.client.Minecraft;

public class KeyStateTracker {

    private final KeyState jump;
    private final KeyState crouch;
    private final KeyState sprint;

    public KeyStateTracker() {
        jump = new KeyState();
        crouch = new KeyState();
        sprint = new KeyState();
    }

    public void update(Minecraft mc) {
        // player.input.jumping includes controller state (set by Controlify's DualInput),
        // whereas mc.options.keyJump.isDown() only reflects the physical keyboard.
        boolean inputJump = mc.options.keyJump.isDown()
                || (mc.player != null && mc.player.input.jumping);
        boolean inputCrouch = mc.options.keyShift.isDown()
                || (mc.player != null && mc.player.input.shiftKeyDown);
        jump.update(inputJump);
        crouch.update(inputCrouch);
        sprint.update(mc.options.keySprint.isDown());
    }

    public void reset() {
        jump.update(false);
        crouch.update(false);
        sprint.update(false);
    }

    public KeyState getJump() {
        return jump;
    }

    public KeyState getCrouch() {
        return crouch;
    }

    public KeyState getSprint() {
        return sprint;
    }
}
