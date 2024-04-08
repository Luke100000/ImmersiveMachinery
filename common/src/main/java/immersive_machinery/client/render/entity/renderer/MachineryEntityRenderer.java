package immersive_machinery.client.render.entity.renderer;

import immersive_aircraft.client.render.entity.renderer.InventoryVehicleRenderer;
import immersive_machinery.entity.MachineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public abstract class MachineryEntityRenderer<T extends MachineEntity> extends InventoryVehicleRenderer<T> {
    public MachineryEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}

