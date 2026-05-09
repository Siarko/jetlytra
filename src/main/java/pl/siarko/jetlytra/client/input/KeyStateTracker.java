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
        jump.update(mc.options.keyJump.isDown());
        crouch.update(mc.options.keyShift.isDown());
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
