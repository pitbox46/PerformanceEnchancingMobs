package github.pitbox46.performanceenhancedmobs.duck;

import github.pitbox46.performanceenhancedmobs.misc.CacheMap;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface LevelDuck {
    CacheMap<Pair<VoxelShape, VoxelShape>, Boolean> performanceEnhancedMobs$getBlockCollisionCacheMap();
}
