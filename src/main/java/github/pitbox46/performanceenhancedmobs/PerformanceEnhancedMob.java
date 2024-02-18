package github.pitbox46.performanceenhancedmobs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PerformanceEnhancedMob.MODID)
public class PerformanceEnhancedMob {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "performanceenhancedmobs";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public PerformanceEnhancedMob() {
    }
}
