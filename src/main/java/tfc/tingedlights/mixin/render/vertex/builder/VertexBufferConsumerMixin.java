package tfc.tingedlights.mixin.render.vertex.builder;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.tingedlights.BlockTesselator;
import tfc.tingedlights.TesselationState;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

@Mixin(BufferBuilder.class)
public abstract class VertexBufferConsumerMixin implements VertexBufferConsumerExtensions {
    @Unique
    Color defaultColor = new Color(0, 0, 0);

    @Override
    public void setDefault(Color color) {
        defaultColor = color;
    }

    @Override
    public Color getDefaultColor() {
        return TesselationState.getDefault(defaultColor);
    }

    @Shadow
    public abstract VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha);

    @Shadow
    public abstract void putShort(int pIndex, short pShortValue);

    @Shadow
    public abstract void nextElement();

    @Shadow public abstract VertexFormatElement currentElement();

    @Shadow public abstract void putByte(int pIndex, byte pByteValue);

    // TODO: fix these
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;putShort(IS)V", ordinal = 2), method = "vertex")
    public void swapCrd(BufferBuilder instance, int pIndex, short pShortValue) {
        this.putByte(pIndex, (byte) (Mth.clamp(defaultColor.r(), 0, 1) * 255));
        this.putByte(pIndex + 1, (byte) (Mth.clamp(defaultColor.g(), 0, 1) * 255));
        this.putByte(pIndex + 2, (byte) (Mth.clamp(defaultColor.b(), 0, 1) * 255));
//        instance.putShort(pIndex, (short) pU);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;putShort(IS)V", ordinal = 3), method = "vertex")
    public void swapCrd1(BufferBuilder instance, int pIndex, short pShortValue) {
//        int pU = defaultColor.getRGB();
//        instance.putShort(pIndex + 2, BlockTesselator.packShort(pU, pShortValue));
        this.putByte(pIndex + 1, (byte) (pShortValue));
    }

    public VertexConsumer uv2(int pU, int pV) {
        VertexFormatElement vertexformatelement = this.currentElement();
        if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 2) {
            if (vertexformatelement.getType() == VertexFormatElement.Type.SHORT && vertexformatelement.getCount() == 2) {
//                pU = defaultColor.getRGB();

//                this.putShort(0, (short) pU);
//                this.putShort(2, BlockTesselator.packShort(pU, pV));
                this.putByte(0, (byte) (Mth.clamp(defaultColor.r(), 0, 1) * 255));
                this.putByte(0 + 1, (byte) (Mth.clamp(defaultColor.g(), 0, 1) * 255));
                this.putByte(0 + 2, (byte) (Mth.clamp(defaultColor.b(), 0, 1) * 255));
                this.putByte(0 + 3, (byte) (pV));
                this.nextElement();
            } else {
                throw new IllegalStateException();
            }
        }
        return (VertexConsumer) this;
    }
}
