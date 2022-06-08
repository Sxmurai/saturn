package wtf.saturn.feature.impl.modules.visuals;

import me.bush.eventbus.annotation.EventListener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.EnumParticleTypes;
import wtf.saturn.event.AttackEntityEvent;
import wtf.saturn.feature.cache.impl.module.impl.annotations.Incompatible;
import wtf.saturn.feature.cache.impl.module.impl.Module;
import wtf.saturn.feature.cache.impl.module.impl.ModuleCategory;
import wtf.saturn.feature.impl.modules.performance.NoParticles;
import wtf.saturn.feature.impl.setting.Setting;
import wtf.saturn.util.timing.Stopwatch;
import wtf.saturn.util.timing.TimeFormat;

@Incompatible(NoParticles.class)
public class HitParticles extends Module {
    public HitParticles() {
        super("Hit Particles", ModuleCategory.VISUALS, "Shows more particles when hitting an entity");
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.MAGIC_CRITICALS);
    public final Setting<Integer> multiplier = new Setting<>("Multiplier", 3, 1, 10);
    public final Setting<Double> delay = new Setting<>("Delay", 500.0, 0.0, 1000.0);

    private final Stopwatch stopwatch = new Stopwatch();

    @Override
    protected void onActivated() {
        super.onActivated();
        stopwatch.resetTime();

        mc.thePlayer.sendChatMessage("penis");
    }

    @EventListener
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase base = (EntityLivingBase) event.getEntity();

        if (stopwatch.hasElapsed(delay.getValue().longValue(), true, TimeFormat.MILLISECONDS)) {
            Mode m = mode.getValue();
            if (m.particleType != null) {
                for (int i = 0; i < multiplier.getValue(); ++i) {
                    mc.effectRenderer.emitParticleAtEntity(base, m.particleType);
                }
            } else {

                // TODO: superherofx and blood
                switch (m) {
                    case LIGHTNING:
                        EntityLightningBolt bolt = new EntityLightningBolt(mc.theWorld, base.posX, base.posY, base.posZ);

                        bolt.rotationYaw = 0.0f;
                        bolt.rotationPitch = 0.0f;

                        mc.theWorld.addWeatherEffect(bolt);
                        break;
                }
            }
        }
    }

    public enum Mode {
        CRITICALS(EnumParticleTypes.CRIT),
        MAGIC_CRITICALS(EnumParticleTypes.CRIT_MAGIC),
        SMOKE(EnumParticleTypes.SMOKE_NORMAL),
        NOTE(EnumParticleTypes.NOTE),
        BLOOD,
        LIGHTNING,
        SUPERHERO_FX; // TODO: idea from ZeroDay

        private final EnumParticleTypes particleType;

        Mode() {
            particleType = null;
        }

        Mode(EnumParticleTypes particleType) {
            this.particleType = particleType;
        }
    }
}
