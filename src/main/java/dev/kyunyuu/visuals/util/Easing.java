package dev.kyunyuu.visuals.util;

/**
 * Small collection of easing curves used by the KyunyuuVisuals interface.
 */
public enum Easing {
    LINEAR {
        @Override
        public float apply(float t) {
            return t;
        }
    },
    OUT_CUBIC {
        @Override
        public float apply(float t) {
            float f = 1f - t;
            return 1f - f * f * f;
        }
    },
    OUT_QUART {
        @Override
        public float apply(float t) {
            float f = 1f - t;
            return 1f - f * f * f * f;
        }
    },
    OUT_EXPO {
        @Override
        public float apply(float t) {
            return t >= 1f ? 1f : 1f - (float) Math.pow(2, -10f * t);
        }
    },
    IN_OUT_CUBIC {
        @Override
        public float apply(float t) {
            return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
        }
    },
    OUT_BACK {
        @Override
        public float apply(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1f;
            float f = t - 1f;
            return 1f + c3 * f * f * f + c1 * f * f;
        }
    },
    OUT_ELASTIC {
        @Override
        public float apply(float t) {
            if (t <= 0f) return 0f;
            if (t >= 1f) return 1f;
            float c4 = (2f * (float) Math.PI) / 3f;
            return (float) Math.pow(2, -10f * t) * (float) Math.sin((t * 10f - 0.75f) * c4) + 1f;
        }
    };

    public abstract float apply(float t);

    public float apply(double t) {
        return apply((float) t);
    }
}
