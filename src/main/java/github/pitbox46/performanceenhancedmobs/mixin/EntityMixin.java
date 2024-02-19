package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.ImmutableList;
import github.pitbox46.performanceenhancedmobs.duck.BlockCollisionsDuck;
import net.minecraft.commands.CommandSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(at = @At(value = "HEAD"), method = "collideBoundingBox", cancellable = true)
    private static void collideBoundingBox(Entity pEntity, Vec3 pVec, AABB pCollisionBox, Level pLevel, List<VoxelShape> pPotentialHits, CallbackInfoReturnable<Vec3> cir) {
        ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(pPotentialHits.size() + 1);
        if (!pPotentialHits.isEmpty()) {
            builder.addAll(pPotentialHits);
        }

        WorldBorder worldborder = pLevel.getWorldBorder();
        boolean flag = pEntity != null && worldborder.isInsideCloseToBorder(pEntity, pCollisionBox.expandTowards(pVec));
        if (flag) {
            builder.add(worldborder.getCollisionShape());
        }

        BlockCollisions<VoxelShape> blockCollisions = new BlockCollisions<>(pLevel, pEntity, pCollisionBox.expandTowards(pVec), false, (p_286215_, p_286216_) -> p_286216_);

        builder.addAll(((BlockCollisionsDuck<VoxelShape>) blockCollisions).performanceEnhancedMobs$computeList());
        cir.setReturnValue(collideWithShapes(pVec, pCollisionBox, builder.build()));
    }
}
