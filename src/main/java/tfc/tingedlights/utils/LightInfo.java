package tfc.tingedlights.utils;

import net.minecraft.core.BlockPos;
import tfc.tingedlights.api.data.Light;

public record LightInfo(Light light, BlockPos pos, byte brightness) {
}
