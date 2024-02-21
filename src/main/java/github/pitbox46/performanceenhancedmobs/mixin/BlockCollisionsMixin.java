package github.pitbox46.performanceenhancedmobs.mixin;

import github.pitbox46.performanceenhancedmobs.PerformanceEnhancedMob;
import github.pitbox46.performanceenhancedmobs.misc.CacheMap;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(BlockCollisions.class)
public class BlockCollisionsMixin {
    @Unique
    private final static Map<Pair<VoxelShape, VoxelShape>, Boolean> performanceEnhancedMobs$CACHE = new CacheMap<>();

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"), method = "computeNext")
    private boolean joinIsNotEmpty(VoxelShape shape1, VoxelShape shape2, BooleanOp op) {
        Pair<VoxelShape, VoxelShape> pair = Pair.of(shape1, shape2);
        if (!performanceEnhancedMobs$CACHE.containsKey(pair)) {
            performanceEnhancedMobs$CACHE.put(pair, Shapes.joinIsNotEmpty(shape1, shape2, op));
        }
        return performanceEnhancedMobs$CACHE.get(pair);
    }
}
