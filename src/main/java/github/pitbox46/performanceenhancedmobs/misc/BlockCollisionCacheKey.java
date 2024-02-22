package github.pitbox46.performanceenhancedmobs.misc;

import net.minecraft.world.phys.AABB;

import java.util.Objects;

public class BlockCollisionCacheKey {
    private final int x;
    private final int y;
    private final int z;
    private final int entityID;
    private final AABB entityBox;

    protected Integer hash;

    public BlockCollisionCacheKey(int x, int y, int z, int entityID, AABB entityBox) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityID = entityID;
        this.entityBox = entityBox;
    }


    @Override
    public int hashCode() {
        return hash==null ? (hash = Objects.hash(x & 15, y, z & 15, entityID, hashEntityBox())) : hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this) {
            return true;
        } else if (obj instanceof BlockCollisionCacheKey otherKey) {
            return x==otherKey.x &&
                    y==otherKey.y &&
                    z==otherKey.z &&
                    entityID==otherKey.entityID &&
                    equalsEntityBox(otherKey.entityBox);
        }
        return false;
    }

    public int hashEntityBox() {
        return Float.floatToIntBits((float) entityBox.minX) +
                Float.floatToIntBits((float) entityBox.minY) +
                Float.floatToIntBits((float) entityBox.minZ) +
                Float.floatToIntBits((float) entityBox.maxX) +
                Float.floatToIntBits((float) entityBox.maxY) +
                Float.floatToIntBits((float) entityBox.maxZ);
    }

    public boolean equalsEntityBox(AABB pOther) {
        if (entityBox==pOther) {
            return true;
        } else {
            if (Float.compare((float) pOther.minX, (float) entityBox.minX)!=0) {
                return false;
            } else if (Float.compare((float) pOther.minY, (float) entityBox.minY)!=0) {
                return false;
            } else if (Float.compare((float) pOther.minZ, (float) entityBox.minZ)!=0) {
                return false;
            } else if (Float.compare((float) pOther.maxX, (float) entityBox.maxX)!=0) {
                return false;
            } else if (Float.compare((float) pOther.maxY, (float) entityBox.maxY)!=0) {
                return false;
            } else {
                return Float.compare((float) pOther.maxZ, (float) entityBox.maxZ)==0;
            }
        }
    }
}
