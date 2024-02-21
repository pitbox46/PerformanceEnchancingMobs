package github.pitbox46.performanceenhancedmobs.mixin;

import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity> implements Nameable, EntityAccess, CommandSource, net.minecraftforge.common.extensions.IForgeEntity {
    @Shadow
    private static Vec3 collideWithShapes(Vec3 pDeltaMovement, AABB pEntityBB, List<VoxelShape> pShapes) {
        return null;
    }

    @Unique
    private boolean performanceEnhancedMobs$fireStartedLastTick = false;

    protected EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Inject(at = @At(value = "HEAD"), method = "setSecondsOnFire")
    private void onSetFire(int pSeconds, CallbackInfo ci) {
        performanceEnhancedMobs$fireStartedLastTick = true;
    }

    //Remove unnecessary stream
    //~1.3% of tick time
    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;noneMatch(Ljava/util/function/Predicate;)Z"), method = "move")
    private boolean noneMatchProxy(Stream<?> instance, Predicate<?> predicate) {
        boolean temp = performanceEnhancedMobs$fireStartedLastTick;
        performanceEnhancedMobs$fireStartedLastTick = false;
        return temp;
    }
}
