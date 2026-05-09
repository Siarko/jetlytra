package pl.siarko.jetlytra.client.input;

public class KeyState {

    private boolean state = false, previousState = false;

    public void update(boolean state) {
        previousState = this.state;
        this.state = state;
    }

    public boolean changed() {
        return previousState != state;
    }

    public boolean isActive() {
        return state;
    }
}
