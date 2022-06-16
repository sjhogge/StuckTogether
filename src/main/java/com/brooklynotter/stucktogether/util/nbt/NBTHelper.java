package com.brooklynotter.stucktogether.util.nbt;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class NBTHelper {

    public static StringTag serializeUUID(UUID uuid) {
        return StringTag.valueOf(uuid.toString());
    }

    public static UUID deserializeUUID(StringTag stringTag) {
        return UUID.fromString(stringTag.getAsString());
    }

    public static <N extends Tag, S extends INBTSerializable<N>> Function<N, S> deserializer(Supplier<S> initializer) {
        return (nbt) -> {
            S serializable = initializer.get();
            serializable.deserializeNBT(nbt);
            return serializable;
        };
    }

    public static <T, N extends Tag> ListTag serializeCollection(Collection<T> collection, Function<T, N> mapper) {
        ListTag nbt = new ListTag();
        collection.forEach(item -> nbt.add(mapper.apply(item)));
        return nbt;
    }

    public static <T, C extends Collection<T>, N extends Tag> C deserializeCollection(ListTag nbt, Class<N> elementType, Supplier<C> initializer, Function<N, T> mapper) {
        C collection = initializer.get();
        nbt.forEach(tag -> {
            @SuppressWarnings("unchecked")
            N nTag = (N) tag;
            collection.add(mapper.apply(nTag));
        });
        return collection;
    }

}
