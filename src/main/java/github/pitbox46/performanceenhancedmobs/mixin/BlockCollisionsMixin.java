package github.pitbox46.performanceenhancedmobs.mixin;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import github.pitbox46.performanceenhancedmobs.PerformanceEnhancedMob;
import github.pitbox46.performanceenhancedmobs.duck.BlockCollisionsDuck;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.*;
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
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin<T> extends AbstractIterator<T> implements BlockCollisionsDuck<T> {
    @Unique private final ConcurrentHashMap<Long, BlockGetter> chunkCache = new ConcurrentHashMap<>();

    @Unique private static final ExecutorService performanceEnhancedMobs$THREADS = Executors.newFixedThreadPool(2, r -> new Thread(r, "Block Collisions Thread"));

    @Shadow @Nullable protected abstract BlockGetter getChunk(int pX, int pZ);

    @Shadow @Final private BlockPos.MutableBlockPos pos;

    @Shadow @Final private boolean onlySuffocatingBlocks;

    @Shadow @Final private CollisionGetter collisionGetter;

    @Shadow @Final private CollisionContext context;

    @Shadow @Final private AABB box;

    @Shadow @Final private BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> resultProvider;

    @Shadow @Final private VoxelShape entityShape;

    @Unique
    @Override
    public Iterable<T> performanceEnhancedMobs$computeList() {
        double halfway = box.minY + (box.maxY - box.minY) / 2;

        int i = Mth.floor(box.minX - 1.0E-7D) - 1;
        int j = Mth.floor(box.maxX + 1.0E-7D) + 1;
        int k = Mth.floor(box.minY - 1.0E-7D) - 1;
        int l = Mth.floor(box.maxY + 1.0E-7D) + 1;
        int i1 = Mth.floor(box.minZ - 1.0E-7D) - 1;
        int j1 = Mth.floor(box.maxZ + 1.0E-7D) + 1;

        AABB box1 = box.setMaxY(halfway);
        int x1 = i;
        int x2 = j;
        int y1 = k;
        int y2 = Mth.floor(box1.maxY);
        int z1 = i1;
        int z2 = j1;
        Cursor3D cursor1 = new Cursor3D(x1, y1, z1, x2, y2, z2);

        AABB box2 = box.setMinY(halfway);
        y1 = Mth.floor(box2.minY) + 1;
        y2 = Mth.floor(box2.maxY + 1.0E-7D) + 1;
        Cursor3D cursor2 = new Cursor3D(x1, y1, z1, x2, y2, z2);

        for (int x = SectionPos.blockToSectionCoord(i); x <= SectionPos.blockToSectionCoord(j); x++) {
            for (int z = SectionPos.blockToSectionCoord(i1); z <= SectionPos.blockToSectionCoord(j1); z++) {
                BlockGetter blockGetter = getChunk(x, z);
                chunkCache.put(ChunkPos.asLong(x, z), blockGetter == null ? EmptyBlockGetter.INSTANCE : blockGetter);
                synchronized (chunkCache) {
                    chunkCache.notifyAll();
                }
            }
        }

        Future<List<T>> future1 = performanceEnhancedMobs$THREADS.submit(() -> performanceEnhancedMobs$getShapes(cursor1, box1));
        Future<List<T>> future2 = performanceEnhancedMobs$THREADS.submit(() -> performanceEnhancedMobs$getShapes(cursor2, box2));

        try {
            return Iterables.concat(future1.get(), future2.get());
        } catch (InterruptedException | ExecutionException e) {
            PerformanceEnhancedMob.LOGGER.warn(e.getMessage());
            return List.of();
        }
    }

    @Unique
    private List<T> performanceEnhancedMobs$getShapes(Cursor3D cursor, AABB pBox) {
        List<T> list = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while(cursor.advance()) {
            int i = cursor.nextX();
            int j = cursor.nextY();
            int k = cursor.nextZ();
            int l = cursor.getNextType();
            if (l==3) {
                continue;
            }

            BlockGetter blockgetter = this.performanceEnhancedMobs$getCachedChunk(i, k);
            if (blockgetter==null || blockgetter==EmptyBlockGetter.INSTANCE) {
                continue;
            }

            pos.set(i, j, k);
            BlockState blockstate = blockgetter.getBlockState(pos);
            if (this.onlySuffocatingBlocks && !blockstate.isSuffocating(blockgetter, pos) || l==1 && !blockstate.hasLargeCollisionShape() || l==2 && !blockstate.is(Blocks.MOVING_PISTON)) {
                continue;
            }

            VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, pos, this.context);
            if (voxelshape==Shapes.block()) {
                if (!pBox.intersects(i, j, k, i + 1.0D, j + 1.0D, k + 1.0D)) {
                    continue;
                }

                list.add(this.resultProvider.apply(pos, voxelshape.move(i, j, k)));
                continue;
            }

            VoxelShape voxelshape1 = voxelshape.move(i, j, k);
            if (voxelshape1.isEmpty() || !Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND)) {
                continue;
            }

            list.add(this.resultProvider.apply(pos, voxelshape1));
        }
        return list;
    }

    @Unique
    private BlockGetter performanceEnhancedMobs$getCachedChunk(int blockX, int blockZ) {
        long k = ChunkPos.asLong(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ));
        while (!chunkCache.containsKey(k)) {
            synchronized (chunkCache) {
                try {
                    chunkCache.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return chunkCache.get(k);
    }
}
