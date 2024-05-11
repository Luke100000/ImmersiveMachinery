package immersive_machinery.entity;

import immersive_machinery.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class RedstoneSheep extends MachineEntity {
    public RedstoneSheep(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, false);
    }

    @Override
    public Item asItem() {
        return Items.REDSTONE_SHEEP.get();
    }
}
