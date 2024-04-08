package immersive_machinery.entity;

import immersive_machinery.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class TunnelDiggerEntity extends MachineEntity {
    public TunnelDiggerEntity(EntityType<? extends TunnelDiggerEntity> entityType, Level world) {
        super(entityType, world, true);
    }

    @Override
    public Item asItem() {
        return Items.TUNNEL_DIGGER.get();
    }
}
