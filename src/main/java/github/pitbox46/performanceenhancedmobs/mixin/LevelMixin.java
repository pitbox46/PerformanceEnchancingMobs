package github.pitbox46.performanceenhancedmobs.mixin;

import github.pitbox46.performanceenhancedmobs.duck.LevelDuck;
import github.pitbox46.performanceenhancedmobs.misc.CacheMap;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public class LevelMixin implements LevelDuck {
    @Unique
    private final CacheMap<Pair<VoxelShape, VoxelShape>, Boolean> performanceEnhancedMobs$collisionsCacheMap = new CacheMap<>(512);
    
    @Override
    public CacheMap<Pair<VoxelShape, VoxelShape>, Boolean> performanceEnhancedMobs$getBlockCollisionCacheMap() {
        return performanceEnhancedMobs$collisionsCacheMap;
    }
}
