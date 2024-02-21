package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.AbstractIterator;
import github.pitbox46.performanceenhancedmobs.PerformanceEnhancedMob;
import github.pitbox46.performanceenhancedmobs.duck.LevelChunkDuck;
import github.pitbox46.performanceenhancedmobs.misc.BlockCollisionCacheKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin<T> extends AbstractIterator<T> {
    @Unique private int performanceEnhancedMobs$entityID;

    @Shadow @Final private CollisionGetter collisionGetter;

    @Shadow @Final private VoxelShape entityShape;

    @Shadow @Final private Cursor3D cursor;

    @Shadow @Nullable protected abstract BlockGetter getChunk(int pX, int pZ);

    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Shadow @Final private boolean onlySuffocatingBlocks;

    @Shadow @Final private CollisionContext context;

    @Shadow @Final private AABB box;

    @Shadow @Final private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    @Inject(at = @At(value = "RETURN"), method = "<init>")
    private void afterConstructor(CollisionGetter pCollisionGetter, Entity pEntity, AABB pBox, boolean pOnlySuffocatingBlocks, BiFunction pResultProvider, CallbackInfo ci) {
        performanceEnhancedMobs$entityID = pEntity == null ? Integer.MIN_VALUE : pEntity.getId();
    }

    /**
     * Provides a minimal performance increase. May be higher for areas with complex voxelshapes
     * @author pitbox46
     * @reason More performant than using a redirect and an Injection is not feasible due to no implementation for *continue*
     */
    @Overwrite
    protected T computeNext() {
        while (this.cursor.advance()) {
            int l = this.cursor.getNextType();
            if (l==3) {
                continue;
            }
            int i = this.cursor.nextX();
            int j = this.cursor.nextY();
            int k = this.cursor.nextZ();

            BlockGetter blockgetter = this.getChunk(i, k);
            if (blockgetter==null) {
                continue;
            }

            this.pos.set(i, j, k);
            BlockState blockstate = blockgetter.getBlockState(this.pos);
            if (this.onlySuffocatingBlocks && !blockstate.isSuffocating(blockgetter, this.pos) || l==1 && !blockstate.hasLargeCollisionShape() || l==2 && !blockstate.is(Blocks.MOVING_PISTON)) {
                continue;
            }

            VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
            if (voxelshape==Shapes.block()) {
                if (!this.box.intersects(i, j, k, i + 1.0D, j + 1.0D, k + 1.0D)) {
                    continue;
                }

                return this.resultProvider.apply(this.pos, voxelshape.move(i, j, k));
            }
            if (voxelshape.isEmpty()) {
                continue;
            }

            VoxelShape voxelshape1 = voxelshape.move(i, j, k);

            boolean flag;
            if (blockgetter instanceof LevelChunkDuck duck) {
                var cache = duck.performanceEnhancedMobs$getBlockCollisionCacheMap();
                BlockCollisionCacheKey currentKey = new BlockCollisionCacheKey(i, j, k, performanceEnhancedMobs$entityID, box);
                if (!cache.containsKey(currentKey)) {
                    cache.put(currentKey, Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND));
                    PerformanceEnhancedMob.notCached++;
                }
                PerformanceEnhancedMob.total++;
                flag = cache.get(currentKey);
            } else {
                flag = Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND);
            }
            if (!flag) {
                continue;
            }

            return this.resultProvider.apply(this.pos, voxelshape1);
        }

        return this.endOfData();
    }
}
