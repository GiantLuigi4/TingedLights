package tfc.tingedlights.api;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.events.DynamicLightSetupEvent;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.TingedLightsItemAttachments;
import tfc.tingedlights.util.Vector5f;
import tfc.tingedlights.utils.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;

public class DynamicLightApi {
	private static final Color[] colors = new Color[16];
	
	static {
		Arrays.fill(colors, Color.BLACK);
	}
	
	public static Vector5f[] getVecs() {
		return vecs.toArray(new Vector5f[0]);
	}
	
	public void setDynamicLight(
			Item block,
			BiFunction<ItemStack, Entity, Light> lightCreation,
			BiFunction<ItemStack, Entity, Integer> brightnessGetter
	) {
		if (block instanceof TingedLightsItemAttachments attachments) {
			attachments.setFunctions(lightCreation, brightnessGetter);
		}
	}
	
	public static void postSetup() {
		DynamicLightApi api = new DynamicLightApi();
		DynamicLightSetupEvent event = new DynamicLightSetupEvent(api);
		MinecraftForge.EVENT_BUS.post(event);
	}
	
	private static final NativeImage img = new NativeImage(15, 100, false);
	private static final DynamicTexture texture = new DynamicTexture(img);
	
	private static final ArrayList<Vector5f> vecs = new ArrayList<>();
	
	public static void tick() {
		if (Minecraft.getInstance().player == null)
			return;
		
		if (Config.GeneralOptions.useLightmap)
			for (int i = 0; i < 15; i++)
				for (int i1 = 0; i1 < 100; i1++)
					img.setPixelRGBA(i, i1, 0);
		
		Player player = Minecraft.getInstance().player;
		Arrays.fill(colors, Color.BLACK);
		
		for (InteractionHand value : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(value);
			
			if (stack.getItem() instanceof TingedLightsItemAttachments attachments) {
				Light light = attachments.createLight(stack, player);
				
				if (light != null) {
					int brightness = attachments.getBrightness(stack, player);
					
					for (int i = 0; i < 15; i++) {
						if (i > brightness) continue;
						
						Color color = light.getColor((byte) ((brightness - i) + 1));
						color = new Color(
								Math.min(colors[i].r() + color.r(), 1),
								Math.min(colors[i].g() + color.g(), 1),
								Math.min(colors[i].b() + color.b(), 1)
						);
						if (Config.GeneralOptions.useLightmap) {
							colors[i] = color;
							img.setPixelRGBA(i, 0, color.getRGB());
						} else {
							colors[i] = new Color(
									Math.min(colors[i].r() + color.r(), 1),
									Math.min(colors[i].g() + color.g(), 1),
									Math.min(colors[i].b() + color.b(), 1)
							);
						}
					}
				}
			}
		}
		
		
		if (Config.GeneralOptions.useLightmap) {
			vecs.clear();
			
			HashMap<Light, Integer> lights = new HashMap<>();
			for (Entity entity : Minecraft.getInstance().level.getEntities(Minecraft.getInstance().player, new AABB(
					Minecraft.getInstance().player.getX() - 100,
					Minecraft.getInstance().player.getY() - 100,
					Minecraft.getInstance().player.getZ() - 100,
					Minecraft.getInstance().player.getX() + 100,
					Minecraft.getInstance().player.getY() + 100,
					Minecraft.getInstance().player.getZ() + 100
			))) {
				if (entity instanceof ItemEntity item) {
					if (item.getItem().getItem() instanceof TingedLightsItemAttachments attachments) {
						int brightness = attachments.getBrightness(item.getItem(), item);
						
						if (brightness != 0) {
							Light light = attachments.createLight(item.getItem(), item);
							if (!lights.containsKey(light)) {
								lights.put(light, lights.size());
								
								for (int i = 0; i < 15; i++) {
									Color color = light.getColor((byte) ((15 - i)));
									img.setPixelRGBA(i, lights.size(), color.getRGB());
								}
								
								if (lights.size() == 99) continue;
							} else continue;
							
							vecs.add(new Vector5f(
									(float) item.getPosition(0).x,
									(float) item.getPosition(0).y,
									(float) item.getPosition(0).z,
									lights.get(light),
									attachments.getBrightness(item.getItem(), item)
							));
						}
					}
				} else if (entity instanceof Zombie || entity instanceof Player) {
					for (InteractionHand value : InteractionHand.values()) {
						ItemStack stack = ((LivingEntity) entity).getItemInHand(value);
						
						if (stack.getItem() instanceof TingedLightsItemAttachments attachments) {
							Light light = attachments.createLight(stack, player);
							
							if (light != null) {
								if (!lights.containsKey(light)) {
									lights.put(light, lights.size());
									
									for (int i = 0; i < 15; i++) {
										Color color = light.getColor((byte) ((15 - i)));
										img.setPixelRGBA(i, lights.size(), color.getRGB());
									}
									
									if (lights.size() == 99) continue;
								} else continue;
								
								vecs.add(new Vector5f(
										(float) entity.getPosition(0).x,
										(float) entity.getPosition(0).y,
										(float) entity.getPosition(0).z,
										lights.get(light),
										attachments.getBrightness(stack, entity)
								));
							}
						}
					}
				}
			}
			
			texture.upload();
		}
	}
	
	public static Color getColor(int index) {
		return colors[index];
	}
	
	public static int getImg() {
		return texture.getId();
	}
}
