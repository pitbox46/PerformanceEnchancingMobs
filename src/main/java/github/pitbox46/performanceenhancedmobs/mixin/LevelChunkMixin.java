package github.pitbox46.performanceenhancedmobs.mixin;

import github.pitbox46.performanceenhancedmobs.duck.LevelChunkDuck;
import github.pitbox46.performanceenhancedmobs.misc.BlockCollisionCacheKey;
import github.pitbox46.performanceenhancedmobs.misc.CacheMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements net.minecraftforge.common.capabilities.ICapabilityProviderImpl<LevelChunk>, LevelChunkDuck {
    @Unique
    private final CacheMap<BlockCollisionCacheKey, Boolean> performanceEnhancedMobs$collisionsCacheMap = new CacheMap<>(512);

    public LevelChunkMixin(ChunkPos pChunkPos, UpgradeData pUpgradeData, LevelHeightAccessor pLevelHeightAccessor, Registry<Biome> pBiomeRegistry, long pInhabitedTime, @Nullable LevelChunkSection[] pSections, @Nullable BlendingData pBlendingData) {
        super(pChunkPos, pUpgradeData, pLevelHeightAccessor, pBiomeRegistry, pInhabitedTime, pSections, pBlendingData);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"), method = "setBlockState")
    private void onSetSectionBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving, CallbackInfoReturnable<BlockState> cir) {
        performanceEnhancedMobs$collisionsCacheMap.clear();
    }

    @Override
    public CacheMap<BlockCollisionCacheKey, Boolean> performanceEnhancedMobs$getBlockCollisionCacheMap() {
        return performanceEnhancedMobs$collisionsCacheMap;
    }
}
