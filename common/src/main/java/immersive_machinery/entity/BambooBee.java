package immersive_machinery.entity;

import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_machinery.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BambooBee extends MachineEntity {
    private final List<Configuration> configurations = new ArrayList<>();

    public BambooBee(EntityType<? extends MachineEntity> entityType, Level world) {
        super(entityType, world, false);

        for (int i = 0; i < 18; i++) {
            configurations.add(new Configuration());
        }
    }

    @Override
    public void setAnimationVariables(float tickDelta) {
        super.setAnimationVariables(tickDelta);

        BBAnimationVariables.set("grabber", 0.0f);
    }

    @Override
    public Item asItem() {
        return Items.BAMBOO_BEE.get();
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public static class Configuration {
        private ItemStack containerFilter = ItemStack.EMPTY;
        private boolean blacklist;
        private boolean compareTag;
        private final ItemStack[] filters = new ItemStack[18];

        public Configuration() {
            for (int i = 0; i < 18; i++) {
                filters[i] = ItemStack.EMPTY;
            }
        }

        public void encode(FriendlyByteBuf b) {
            b.writeItem(containerFilter);
            b.writeBoolean(blacklist);
            b.writeBoolean(compareTag);
            for (ItemStack filter : filters) {
                b.writeItem(filter);
            }
        }

        public void decode(FriendlyByteBuf b) {
            containerFilter = b.readItem();
            blacklist = b.readBoolean();
            compareTag = b.readBoolean();
            for (int i = 0; i < 18; i++) {
                filters[i] = b.readItem();
            }
        }
    }
}
