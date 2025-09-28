package net.jeremy.gardenkingmod.client.render;

import org.jetbrains.annotations.Nullable;

import net.jeremy.gardenkingmod.GardenKingMod;
import net.jeremy.gardenkingmod.ModBlocks;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.jeremy.gardenkingmod.client.model.ScarecrowModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.entity.EquipmentSlot;

public final class ScarecrowRenderHelper {
    private static final Identifier BASE_TEXTURE = new Identifier(
            GardenKingMod.MOD_ID,
            "textures/entity/scarecrow/scarecrow.png"
    );

    private final ScarecrowModel baseModel;
    private final BipedEntityModel<?> bodyModel;
    private final BipedEntityModel<?> innerArmorModel;
    private final BipedEntityModel<?> outerArmorModel;

    public ScarecrowRenderHelper(ModelPart baseModelPart, ModelPart bodyModelPart,
            ModelPart innerArmorModelPart, ModelPart outerArmorModelPart) {
        this.baseModel = new ScarecrowModel(baseModelPart);
        this.bodyModel = new BipedEntityModel<>(bodyModelPart);
        this.innerArmorModel = new BipedEntityModel<>(innerArmorModelPart);
        this.outerArmorModel = new BipedEntityModel<>(outerArmorModelPart);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
            ScarecrowEquipment equipment, @Nullable World world) {
        VertexConsumer baseConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BASE_TEXTURE));
        this.baseModel.render(matrices, baseConsumer, light, overlay, 1.0f, 1.0f, 1.0f, 1.0f);

        renderHeadLayer(matrices, vertexConsumers, light, overlay, equipment.head(), world);
        renderHeadLayer(matrices, vertexConsumers, light, overlay, equipment.hat(), world);
        renderChestLayer(matrices, vertexConsumers, light, overlay, equipment.chest(), world);
        renderPitchfork(matrices, vertexConsumers, light, overlay, equipment.pitchfork());
    }

    private void renderHeadLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
            ItemStack stack, @Nullable World world) {
        if (stack.isEmpty()) {
            return;
        }

        Item item = stack.getItem();
        if (item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.HEAD) {
            renderArmor(stack, armorItem, matrices, vertexConsumers, light, overlay, world);
            return;
        }

        matrices.push();
        applyScarecrowPose(this.bodyModel);
        this.bodyModel.head.rotate(matrices);
        matrices.translate(0.0F, -0.25F, 0.0F);
        matrices.scale(0.75F, 0.75F, 0.75F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.renderItem(stack, ModelTransformationMode.HEAD,
                light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    private void renderChestLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
            ItemStack stack, @Nullable World world) {
        if (stack.isEmpty()) {
            return;
        }

        Item item = stack.getItem();
        if (item instanceof ArmorItem armorItem && armorItem.getSlotType() == EquipmentSlot.CHEST) {
            renderArmor(stack, armorItem, matrices, vertexConsumers, light, overlay, world);
            return;
        }

        matrices.push();
        applyScarecrowPose(this.bodyModel);
        this.bodyModel.body.rotate(matrices);
        matrices.translate(0.0F, 0.2F, -0.25F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(0.6F, 0.6F, 0.6F);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED,
                light, overlay, matrices, vertexConsumers, world, 0);
        matrices.pop();
    }

    private void renderPitchfork(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
            ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        matrices.push();
        applyScarecrowPose(this.bodyModel);
        this.bodyModel.rightArm.rotate(matrices);
        matrices.translate(-0.05F, 0.45F, -0.35F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.scale(0.8F, 0.8F, 0.8F);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND,
                light, overlay, matrices, vertexConsumers, null, 0);
        matrices.pop();
    }

    private void renderArmor(ItemStack stack, ArmorItem armorItem, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, int overlay, @Nullable World world) {
        BipedEntityModel<?> model = getModelForSlot(armorItem);
        applyScarecrowPose(model);
        setModelVisibility(model, armorItem);

        boolean isLeggings = armorItem.getSlotType() == EquipmentSlot.LEGS;
        Identifier armorTexture = getArmorTexture(armorItem, isLeggings, false);
        VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                RenderLayer.getArmorCutoutNoCull(armorTexture), false, stack.hasGlint());
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        if (armorItem instanceof DyeableArmorItem dyeable) {
            int color = dyeable.getColor(stack);
            red = (float) (color >> 16 & 0xFF) / 255.0F;
            green = (float) (color >> 8 & 0xFF) / 255.0F;
            blue = (float) (color & 0xFF) / 255.0F;
        }
        model.render(matrices, vertexConsumer, light, overlay, red, green, blue, 1.0F);

        if (armorItem instanceof DyeableArmorItem) {
            Identifier overlayTexture = getArmorTexture(armorItem, isLeggings, true);
            VertexConsumer overlayConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
                    RenderLayer.getArmorCutoutNoCull(overlayTexture), false, stack.hasGlint());
            model.render(matrices, overlayConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }

    }

    private BipedEntityModel<?> getModelForSlot(ArmorItem armorItem) {
        return armorItem.getSlotType() == EquipmentSlot.LEGS ? this.innerArmorModel : this.outerArmorModel;
    }

    private void setModelVisibility(BipedEntityModel<?> model, ArmorItem armorItem) {
        model.setVisible(false);
        EquipmentSlot slot = armorItem.getSlotType();
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    private Identifier getArmorTexture(ArmorItem armorItem, boolean leggings, boolean overlay) {
        String materialName = armorItem.getMaterial().getName();
        Identifier id = new Identifier(materialName);
        String path = String.format("textures/models/armor/%s_layer_%d%s.png",
                id.getPath(), leggings ? 2 : 1, overlay ? "_overlay" : "");
        return new Identifier(id.getNamespace(), path);
    }

    private static void applyScarecrowPose(BipedEntityModel<?> model) {
        model.head.pitch = 0.0F;
        model.head.yaw = 0.0F;
        model.head.roll = 0.0F;
        model.body.pitch = 0.0F;
        model.body.yaw = 0.0F;
        model.body.roll = 0.0F;
        model.rightArm.pitch = 0.0F;
        model.rightArm.yaw = 0.0F;
        model.rightArm.roll = (float) Math.PI / 2.0F;
        model.leftArm.pitch = 0.0F;
        model.leftArm.yaw = 0.0F;
        model.leftArm.roll = -(float) Math.PI / 2.0F;
        model.rightLeg.pitch = 0.0F;
        model.rightLeg.yaw = 0.0F;
        model.rightLeg.roll = 0.0F;
        model.leftLeg.pitch = 0.0F;
        model.leftLeg.yaw = 0.0F;
        model.leftLeg.roll = 0.0F;
        model.hat.copyTransform(model.head);
    }

    public record ScarecrowEquipment(ItemStack hat, ItemStack head, ItemStack chest, ItemStack pitchfork) {
        public static final ScarecrowEquipment EMPTY = new ScarecrowEquipment(ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY);

        public static ScarecrowEquipment fromBlockEntity(ScarecrowBlockEntity entity) {
            return new ScarecrowEquipment(
                    entity.getEquippedHat(),
                    entity.getEquippedHead(),
                    entity.getEquippedChest(),
                    entity.getEquippedPitchfork()
            );
        }

        public static ScarecrowEquipment fromItemStack(ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return EMPTY;
            }
            if (blockItem.getBlock() != ModBlocks.SCARECROW_BLOCK) {
                return EMPTY;
            }
            NbtCompound nbt = BlockItem.getBlockEntityNbt(stack);
            if (nbt == null) {
                return EMPTY;
            }
            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(ScarecrowBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);
            Inventories.readNbt(nbt, inventory);
            return new ScarecrowEquipment(
                    inventory.get(ScarecrowBlockEntity.SLOT_HAT),
                    inventory.get(ScarecrowBlockEntity.SLOT_HEAD),
                    inventory.get(ScarecrowBlockEntity.SLOT_CHEST),
                    inventory.get(ScarecrowBlockEntity.SLOT_PITCHFORK)
            );
        }
    }

    public static ScarecrowRenderHelper createDefault(BlockEntityRendererFactory.Context context) {
        ModelPart base = context.getLayerModelPart(ScarecrowModel.LAYER_LOCATION);
        ModelPart body = context.getLayerModelPart(EntityModelLayers.PLAYER);
        ModelPart inner = context.getLayerModelPart(EntityModelLayers.PLAYER_INNER_ARMOR);
        ModelPart outer = context.getLayerModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR);
        return new ScarecrowRenderHelper(base, body, inner, outer);
    }

    public static ScarecrowRenderHelper createDefault(MinecraftClient client) {
        ModelPart base = client.getEntityModelLoader().getModelPart(ScarecrowModel.LAYER_LOCATION);
        ModelPart body = client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER);
        ModelPart inner = client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR);
        ModelPart outer = client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR);
        return new ScarecrowRenderHelper(base, body, inner, outer);
    }
}
