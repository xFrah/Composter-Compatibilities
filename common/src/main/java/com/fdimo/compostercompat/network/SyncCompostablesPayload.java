package com.fdimo.compostercompat.network;

import com.fdimo.compostercompat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncCompostablesPayload(List<Entry> entries) {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "sync_compostables");

    public record Entry(int itemId, float probability, int remainderId) {}

    public SyncCompostablesPayload(FriendlyByteBuf buf) {
        this(readEntries(buf));
    }

    private static List<Entry> readEntries(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int itemId = buf.readVarInt();
            float prob = buf.readFloat();
            int remainderId = buf.readVarInt();
            list.add(new Entry(itemId, prob, remainderId));
        }
        return list;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entries.size());
        for (Entry entry : this.entries) {
            buf.writeVarInt(entry.itemId());
            buf.writeFloat(entry.probability());
            buf.writeVarInt(entry.remainderId());
        }
    }
}

