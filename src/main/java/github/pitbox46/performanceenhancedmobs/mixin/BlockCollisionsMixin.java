package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.AbstractIterator;
import github.pitbox46.performanceenhancedmobs.duck.BlockCollisionsDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin<T> extends AbstractIterator<T> implements BlockCollisionsDuck {
    @Shadow @Final private Cursor3D cursor;

    @Shadow @Nullable protected abstract BlockGetter getChunk(int pX, int pZ);

    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Shadow @Final private boolean onlySuffocatingBlocks;

    @Shadow @Final private CollisionGetter collisionGetter;

    @Shadow @Final private CollisionContext context;

    @Shadow @Final private AABB box;

    @Shadow @Final private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    @Shadow @Final private VoxelShape entityShape;

    @Override
    public List<VoxelShape> performanceEnchancedMobs$computeList() {
        double halfway = box.minY + (box.maxY - box.minY) / 2;
        AABB box1 = box.setMaxY(halfway);
        int x1 = Mth.floor(box1.minX - 1.0E-7D) - 1;
        int x2 = Mth.floor(box1.maxX + 1.0E-7D) + 1;
        int y1 = Mth.floor(box1.minY - 1.0E-7D) - 1;
        int y2 = Mth.floor(box1.maxY);
        int z1 = Mth.floor(box1.minZ - 1.0E-7D) - 1;
        int z2 = Mth.floor(box1.maxZ + 1.0E-7D) + 1;
        Cursor3D cursor1 = new Cursor3D(x1, y1, z1, x2, y2, z2);

        AABB box2 = box.setMinY(halfway);
        x1 = Mth.floor(box2.minX - 1.0E-7D) - 1;
        x2 = Mth.floor(box2.maxX + 1.0E-7D) + 1;
        y1 = Mth.floor(box2.minY) + 1;
        y2 = Mth.floor(box2.maxY + 1.0E-7D) + 1;
        z1 = Mth.floor(box2.minZ - 1.0E-7D) - 1;
        z2 = Mth.floor(box2.maxZ + 1.0E-7D) + 1;
        Cursor3D cursor2 = new Cursor3D(x1, y1, z1, x2, y2, z2);

        Future<List<VoxelShape>> secondBatch = new FutureTask<>(() -> {
            return null;
        });
        
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

                cir.setReturnValue(this.resultProvider.apply(this.pos, voxelshape.move(i, j, k)));
            }

            VoxelShape voxelshape1 = voxelshape.move(i, j, k);
            if (voxelshape1.isEmpty() || !Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND)) {
                continue;
            }

            cir.setReturnValue(this.resultProvider.apply(this.pos, voxelshape1));
        }
    }
}
