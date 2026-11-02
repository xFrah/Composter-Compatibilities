package com.fdimo.compostercompat.network;

import com.fdimo.compostercompat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SyncCompostablesPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncCompostablesPayload> TYPE = createType(Constants.MOD_ID, "sync_compostables");

    public static final StreamCodec<FriendlyByteBuf, SyncCompostablesPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SyncCompostablesPayload::write, SyncCompostablesPayload::new);

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    private static CustomPacketPayload.Type<SyncCompostablesPayload> createType(String namespace, String path) {
        try {
            Class<?> locClass;
            try {
                locClass = Class.forName("net.minecraft.resources.ResourceLocation");
            } catch (ClassNotFoundException e) {
                locClass = Class.forName("net.minecraft.resources.Identifier");
            }
            Object loc = locClass.getMethod("fromNamespaceAndPath", String.class, String.class).invoke(null, namespace, path);
            java.lang.reflect.Constructor<?> ctor = CustomPacketPayload.Type.class.getDeclaredConstructor(locClass);
            return (CustomPacketPayload.Type<SyncCompostablesPayload>) ctor.newInstance(loc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create packet payload type", e);
        }
    }
}
