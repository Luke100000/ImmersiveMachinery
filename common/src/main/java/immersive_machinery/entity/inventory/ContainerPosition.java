package immersive_machinery.entity.inventory;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public final class ContainerPosition {
    public static Codec<ContainerPosition> CODEC = Codec.withAlternative(CompoundTag.CODEC, TagParser.FLATTENED_CODEC)
            .xmap(ContainerPosition::new, ContainerPosition::toTag);

    private final BlockPos pos;
    private final String name;
    private boolean input;

    public ContainerPosition(BlockPos pos, String name, boolean input) {
        this.pos = pos;
        this.name = name;
        this.input = input;
    }

    public ContainerPosition(CompoundTag tag) {
        pos = BlockPos.of(tag.getLongOr("pos", 0L));
        name = tag.getStringOr("name", "");
        input = tag.getBooleanOr("input", false);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", getPos());
        tag.putString("name", name());
        tag.putBoolean("input", input());
        return tag;
    }

    public long getPos() {
        return pos().asLong();
    }

    public BlockPos pos() {
        return pos;
    }

    public String name() {
        return name;
    }

    public boolean input() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }
}
