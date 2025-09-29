package net.jeremy.gardenkingmod.client.render.item;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.jeremy.gardenkingmod.ModBlocks;
import net.jeremy.gardenkingmod.block.ward.ScarecrowBlockEntity;
import net.jeremy.gardenkingmod.client.render.ScarecrowRenderHelper;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.RotationAxis;

public class ScarecrowItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private ScarecrowRenderHelper renderHelper;

    public ScarecrowItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);
        matrices.scale(ScarecrowRenderHelper.DEFAULT_RENDER_SCALE,
                ScarecrowRenderHelper.DEFAULT_RENDER_SCALE,
                ScarecrowRenderHelper.DEFAULT_RENDER_SCALE);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));

        if (mode != null) {
            switch (mode) {
                case GUI:
                    matrices.scale(0.35f, 0.35f, 0.35f);
                    matrices.translate(0.0, 2.7, 0.0);
                    break;
                case GROUND:
                    matrices.scale(0.35f, 0.35f, 0.35f);
                    matrices.translate(0.0, 1.5, 0.0);
                    break;
                case FIXED:
                    matrices.scale(0.4f, 0.4f, 0.4f);
                    matrices.translate(0.0, 2.3, 0.0);
                    break;
                case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                     THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND:
                    matrices.scale(0.25F, 0.25F, 0.25F);
                    matrices.translate(0.0F, 2.0F, 0.0F);
                    break;
                default: /* keep block-sized scale for pedestal/world */
                    break;
            }
        }

        if (this.renderHelper == null) {
            this.renderHelper = ScarecrowRenderHelper.createDefault(MinecraftClient.getInstance());
        }

        applyEquipmentFromItem(stack);
        this.renderHelper.render(matrices, vertexConsumers, light, overlay);

        matrices.pop();
    }

    private void applyEquipmentFromItem(ItemStack stack) {
        ItemStack hat = ItemStack.EMPTY;
        ItemStack head = ItemStack.EMPTY;
        ItemStack chest = ItemStack.EMPTY;
        ItemStack pants = ItemStack.EMPTY;
        ItemStack pitchfork = ItemStack.EMPTY;

        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == ModBlocks.SCARECROW_BLOCK) {
            NbtCompound nbt = BlockItem.getBlockEntityNbt(stack);
            if (nbt != null) {
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(ScarecrowBlockEntity.INVENTORY_SIZE, ItemStack.EMPTY);
                Inventories.readNbt(nbt, inventory);
                hat = inventory.get(ScarecrowBlockEntity.SLOT_HAT);
                head = inventory.get(ScarecrowBlockEntity.SLOT_HEAD);
                chest = inventory.get(ScarecrowBlockEntity.SLOT_CHEST);
                pants = inventory.get(ScarecrowBlockEntity.SLOT_PANTS);
                pitchfork = inventory.get(ScarecrowBlockEntity.SLOT_PITCHFORK);
            }
        }

        this.renderHelper.setHatStack(hat);
        this.renderHelper.setHeadStack(head);
        this.renderHelper.setChestStack(chest);
        this.renderHelper.setPantsStack(pants);
        this.renderHelper.setPitchforkStack(pitchfork);
    }
}
