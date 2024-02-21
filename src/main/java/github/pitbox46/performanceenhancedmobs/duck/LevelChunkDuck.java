package github.pitbox46.performanceenhancedmobs.duck;

import github.pitbox46.performanceenhancedmobs.misc.BlockCollisionCacheKey;
import github.pitbox46.performanceenhancedmobs.misc.CacheMap;

public interface LevelChunkDuck {
    CacheMap<BlockCollisionCacheKey, Boolean> performanceEnhancedMobs$getBlockCollisionCacheMap();
}
