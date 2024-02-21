package github.pitbox46.performanceenhancedmobs.misc;

import net.minecraft.world.phys.AABB;

public record BlockCollisionCacheKey(int x, int y, int z, int entityID, AABB entityBox) {

    @Override
    public int hashCode() {
        return x + y + z + entityID + entityBox.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BlockCollisionCacheKey otherKey) {
            return x == otherKey.x &&
                    y == otherKey.y &&
                    z == otherKey.z &&
                    entityID == otherKey.entityID &&
                    entityBox.equals(otherKey.entityBox);
        }
        return false;
    }
}
