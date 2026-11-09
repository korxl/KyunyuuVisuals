package dev.kyunyuu.visuals.util;

/**
 * Frame-rate independent interpolator used for every animation in the client.
 * The value chases {@link #target} with an exponential falloff so animations
 * look identical at 60, 144 or 300 fps.
 */
public final class Anim {
    private float value;
    private float target;
    private float speed;
    private long lastUpdate = System.nanoTime();
    private Easing easing = Easing.LINEAR;

    public Anim(float initial) {
        this(initial, 14f);
    }

    public Anim(float initial, float speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public Anim easing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public void target(float target) {
        this.target = target;
    }

    public void snap(float value) {
        this.value = value;
        this.target = value;
        this.lastUpdate = System.nanoTime();
    }

    public float get() {
        update();
        return value;
    }

    /** Current value without advancing the animation. */
    public float peek() {
        return value;
    }

    public float targetValue() {
        return target;
    }

    public boolean isDone() {
        return Math.abs(target - value) < 0.0015f;
    }

    public void update() {
        long now = System.nanoTime();
        float dt = (now - lastUpdate) / 1_000_000_000f;
        lastUpdate = now;
        if (dt <= 0f) return;
        if (dt > 0.25f) dt = 0.25f;

        if (speed <= 0f) {
            value = target;
            return;
        }
        float factor = 1f - (float) Math.exp(-speed * dt);
        if (easing != Easing.LINEAR) {
            float linear = value + (target - value) * factor;
            float progress = target == value ? 1f : (linear - value) / (target - value);
            float eased = easing.apply(Math.max(0f, Math.min(1f, progress)));
            value = value + (target - value) * eased;
        } else {
            value += (target - value) * factor;
        }
        if (Math.abs(target - value) < 0.0015f) {
            value = target;
        }
    }
}
