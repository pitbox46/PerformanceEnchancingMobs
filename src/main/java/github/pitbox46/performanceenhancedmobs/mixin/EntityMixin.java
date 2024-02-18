package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import github.pitbox46.performanceenhancedmobs.duck.BlockCollisionsDuck;
import net.minecraft.commands.CommandSource;
import net.minecraft.core.Direction;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
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
        Iterable<VoxelShape> shapes;

        BlockCollisionsDuck<VoxelShape> blockCollisions = (BlockCollisionsDuck<VoxelShape>) new BlockCollisions<>(pLevel, pEntity, pCollisionBox.expandTowards(pVec), false, (p_286215_, p_286216_) -> p_286216_);

        WorldBorder worldborder = pLevel.getWorldBorder();
        boolean flag = pEntity != null && worldborder.isInsideCloseToBorder(pEntity, pCollisionBox.expandTowards(pVec));
        if (flag) {
            shapes = Iterables.concat(pPotentialHits, List.of(worldborder.getCollisionShape()), blockCollisions.performanceEnchancedMobs$computeList());
        } else {
            shapes = Iterables.concat(pPotentialHits, blockCollisions.performanceEnchancedMobs$computeList());
        }

        cir.setReturnValue(performanceEnhancedMobs$collideWithShapes(pVec, pCollisionBox, shapes));
    }

    @Unique
    private static Vec3 performanceEnhancedMobs$collideWithShapes(Vec3 pDeltaMovement, AABB pEntityBB, Iterable<VoxelShape> pShapes) {
        double d0 = pDeltaMovement.x;
        double d1 = pDeltaMovement.y;
        double d2 = pDeltaMovement.z;
        if (d1 != 0.0D) {
            d1 = Shapes.collide(Direction.Axis.Y, pEntityBB, pShapes, d1);
            if (d1 != 0.0D) {
                pEntityBB = pEntityBB.move(0.0D, d1, 0.0D);
            }
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);
        if (flag && d2 != 0.0D) {
            d2 = Shapes.collide(Direction.Axis.Z, pEntityBB, pShapes, d2);
            if (d2 != 0.0D) {
                pEntityBB = pEntityBB.move(0.0D, 0.0D, d2);
            }
        }

        if (d0 != 0.0D) {
            d0 = Shapes.collide(Direction.Axis.X, pEntityBB, pShapes, d0);
            if (!flag && d0 != 0.0D) {
                pEntityBB = pEntityBB.move(d0, 0.0D, 0.0D);
            }
        }

        if (!flag && d2 != 0.0D) {
            d2 = Shapes.collide(Direction.Axis.Z, pEntityBB, pShapes, d2);
        }

        return new Vec3(d0, d1, d2);
    }
}
