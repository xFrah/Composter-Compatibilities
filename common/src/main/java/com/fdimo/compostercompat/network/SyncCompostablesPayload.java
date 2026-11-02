package com.fdimo.compostercompat.network;

import com.fdimo.compostercompat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncCompostablesPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncCompostablesPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sync_compostables"));

    public static final StreamCodec<FriendlyByteBuf, SyncCompostablesPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncCompostablesPayload::write, SyncCompostablesPayload::new);

    public record Entry(ResourceLocation itemId, float probability, ResourceLocation remainderId) {}

    public SyncCompostablesPayload(FriendlyByteBuf buf) {
        this(readEntries(buf));
    }

    private static List<Entry> readEntries(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Entry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation itemId = buf.readResourceLocation();
            float prob = buf.readFloat();
            ResourceLocation remainderId = buf.readBoolean() ? buf.readResourceLocation() : null;
            list.add(new Entry(itemId, prob, remainderId));
        }
        return list;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entries.size());
        for (Entry entry : this.entries) {
            buf.writeResourceLocation(entry.itemId());
            buf.writeFloat(entry.probability());
            buf.writeBoolean(entry.remainderId() != null);
            if (entry.remainderId() != null) {
                buf.writeResourceLocation(entry.remainderId());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

