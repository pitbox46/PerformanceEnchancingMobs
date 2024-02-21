package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.AbstractIterator;
import github.pitbox46.performanceenhancedmobs.duck.LevelDuck;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
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

import javax.annotation.Nullable;
import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin<T> extends AbstractIterator<T> {
    @Shadow @Final private CollisionGetter collisionGetter;

    @Shadow @Final private VoxelShape entityShape;

    @Shadow @Final private Cursor3D cursor;

    @Shadow @Nullable protected abstract BlockGetter getChunk(int pX, int pZ);

    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Shadow @Final private boolean onlySuffocatingBlocks;

    @Shadow @Final private CollisionContext context;

    @Shadow @Final private AABB box;

    @Shadow @Final private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    /**
     * @author pitbox46
     * @reason More performant than using a redirect and an Injection is not feasible due to no implementation for *continue*
     */
    @Overwrite
    protected T computeNext() {
        while(true) {
            if (this.cursor.advance()) {
                int i = this.cursor.nextX();
                int j = this.cursor.nextY();
                int k = this.cursor.nextZ();
                int l = this.cursor.getNextType();
                if (l == 3) {
                    continue;
                }

                BlockGetter blockgetter = this.getChunk(i, k);
                if (blockgetter == null) {
                    continue;
                }

                this.pos.set(i, j, k);
                BlockState blockstate = blockgetter.getBlockState(this.pos);
                if (this.onlySuffocatingBlocks && !blockstate.isSuffocating(blockgetter, this.pos) || l == 1 && !blockstate.hasLargeCollisionShape() || l == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
                    continue;
                }

                VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
                if (voxelshape == Shapes.block()) {
                    if (!this.box.intersects(i, j, k, i + 1.0D, j + 1.0D, k + 1.0D)) {
                        continue;
                    }

                    return this.resultProvider.apply(this.pos, voxelshape.move(i, j, k));
                }

                VoxelShape voxelshape1 = voxelshape.move(i, j, k);

                if (voxelshape1.isEmpty()) {
                    continue;
                }

                boolean flag;
                if (collisionGetter instanceof LevelDuck duck) {
                    var cacheMap = duck.performanceEnhancedMobs$getBlockCollisionCacheMap();
                    Pair<VoxelShape, VoxelShape> pair = Pair.of(voxelshape1, this.entityShape);
                    if (!cacheMap.containsKey(pair)) {
                        cacheMap.put(pair, Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND));
                    }
                    flag = cacheMap.get(pair);
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
}
