package dev.gideonwhite1029.horizon.bytebuf;

import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.List;

public interface Bytebuf {

    /**
     * Creates a new Bytebuf with the specified size.
     *
     * @param size the size of the Bytebuf
     * @return a new Bytebuf instance
     */
    static Bytebuf buf(int size) {
        return Bukkit.getBytebufManager().newBytebuf(size);
    }

    /**
     * Creates a new Bytebuf with a default size of 128.
     *
     * @return a new Bytebuf instance
     */
    static Bytebuf buf() {
        return buf(128);
    }

    /**
     * Creates a new Bytebuf from the given byte array.
     *
     * @param bytes the byte array to convert to a Bytebuf
     * @return a new Bytebuf instance
     */
    static Bytebuf of(byte[] bytes) {
        return Bukkit.getBytebufManager().toBytebuf(bytes);
    }

    /**
     * Converts the Bytebuf to a byte array.
     *
     * @return the byte array representation of the Bytebuf
     */
    byte[] toArray();

    /**
     * Skips the specified number of bytes.
     *
     * @param i the number of bytes to skip
     * @return the Bytebuf instance
     */
    Bytebuf skipBytes(int i);

    /**
     * Gets the current reader index.
     *
     * @return the reader index
     */
    int readerIndex();

    /**
     * Sets the reader index.
     *
     * @param i the new reader index
     * @return the Bytebuf instance
     */
    Bytebuf readerIndex(int i);

    /**
     * Gets the current writer index.
     *
     * @return the writer index
     */
    int writerIndex();

    /**
     * Sets the writer index.
     *
     * @param i the new writer index
     * @return the Bytebuf instance
     */
    Bytebuf writerIndex(int i);

    /**
     * Resets the reader index to the initial position.
     *
     * @return the Bytebuf instance
     */
    Bytebuf resetReaderIndex();

    /**
     * Resets the writer index to the initial position.
     *
     * @return the Bytebuf instance
     */
    Bytebuf resetWriterIndex();

    /**
     * Writes a byte to the Bytebuf.
     *
     * @param i the byte to write
     * @return the Bytebuf instance
     */
    Bytebuf writeByte(int i);

    /**
     * Reads a byte from the Bytebuf.
     *
     * @return the byte read
     */
    byte readByte();

    /**
     * Writes a boolean to the Bytebuf.
     *
     * @param b the boolean to write
     * @return the Bytebuf instance
     */
    Bytebuf writeBoolean(boolean b);

    /**
     * Reads a boolean from the Bytebuf.
     *
     * @return the boolean read
     */
    boolean readBoolean();

    /**
     * Writes a float to the Bytebuf.
     *
     * @param f the float to write
     * @return the Bytebuf instance
     */
    Bytebuf writeFloat(float f);

    /**
     * Reads a float from the Bytebuf.
     *
     * @return the float read
     */
    float readFloat();

    /**
     * Writes a double to the Bytebuf.
     *
     * @param d the double to write
     * @return the Bytebuf instance
     */
    Bytebuf writeDouble(double d);

    /**
     * Reads a double from the Bytebuf.
     *
     * @return the double read
     */
    double readDouble();

    /**
     * Writes a short to the Bytebuf.
     *
     * @param i the short to write
     * @return the Bytebuf instance
     */
    Bytebuf writeShort(int i);

    /**
     * Reads a short from the Bytebuf.
     *
     * @return the short read
     */
    short readShort();

    /**
     * Writes an int to the Bytebuf.
     *
     * @param i the int to write
     * @return the Bytebuf instance
     */
    Bytebuf writeInt(int i);

    /**
     * Reads an int from the Bytebuf.
     *
     * @return the int read
     */
    int readInt();

    /**
     * Writes a long to the Bytebuf.
     *
     * @param i the long to write
     * @return the Bytebuf instance
     */
    Bytebuf writeLong(long i);

    /**
     * Reads a long from the Bytebuf.
     *
     * @return the long read
     */
    long readLong();

    /**
     * Writes a variable-length int to the Bytebuf.
     *
     * @param i the variable-length int to write
     * @return the Bytebuf instance
     */
    Bytebuf writeVarInt(int i);

    /**
     * Reads a variable-length int from the Bytebuf.
     *
     * @return the variable-length int read
     */
    int readVarInt();

    /**
     * Writes a variable-length long to the Bytebuf.
     *
     * @param i the variable-length long to write
     * @return the Bytebuf instance
     */
    Bytebuf writeVarLong(long i);

    /**
     * Reads a variable-length long from the Bytebuf.
     *
     * @return the variable-length long read
     */
    long readVarLong();

    /**
     * Writes a UUID to the Bytebuf.
     *
     * @param uuid the UUID to write
     * @return the Bytebuf instance
     */
    Bytebuf writeUUID(UUID uuid);

    /**
     * Reads a UUID from the Bytebuf.
     *
     * @return the UUID read
     */
    UUID readUUID();

    /**
     * Writes an enum to the Bytebuf.
     *
     * @param instance the enum instance to write
     * @return the Bytebuf instance
     */
    Bytebuf writeEnum(Enum<?> instance);

    /**
     * Reads an enum from the Bytebuf.
     *
     * @param <T> the type of the enum
     * @param enumClass the class of the enum
     * @return the enum read
     */
    <T extends Enum<T>> T readEnum(Class<T> enumClass);

    /**
     * Writes a UTF-8 string to the Bytebuf.
     *
     * @param utf the string to write
     * @return the Bytebuf instance
     */
    Bytebuf writeUTFString(String utf);

    /**
     * Reads a UTF-8 string from the Bytebuf.
     *
     * @return the string read
     */
    String readUTFString();

    /**
     * Writes a plain text component to the Bytebuf.
     *
     * @param str the plain text component to write
     * @return the Bytebuf instance
     */
    Bytebuf writeComponentPlain(String str);

    /**
     * Reads a plain text component from the Bytebuf.
     *
     * @return the plain text component read
     */
    String readComponentPlain();

    /**
     * Writes a JSON component to the Bytebuf.
     *
     * @param json the JSON component to write
     * @return the Bytebuf instance
     */
    Bytebuf writeComponentJson(JsonElement json);

    /**
     * Reads a JSON component from the Bytebuf.
     *
     * @return the JSON component read
     */
    JsonElement readComponentJson();

    /**
     * Writes an ItemStack to the Bytebuf.
     *
     * @param itemStack the ItemStack to write
     * @return the Bytebuf instance
     */
    Bytebuf writeItemStack(ItemStack itemStack);

    /**
     * Reads an ItemStack from the Bytebuf.
     *
     * @return the ItemStack read
     */
    ItemStack readItemStack();

    /**
     * Writes a list of ItemStacks to the Bytebuf.
     *
     * @param itemStacks the list of ItemStacks to write
     * @return the Bytebuf instance
     */
    Bytebuf writeItemStackList(List<ItemStack> itemStacks);

    /**
     * Reads a list of ItemStacks from the Bytebuf.
     *
     * @return the list of ItemStacks read
     */
    List<ItemStack> readItemStackList();

    /**
     * Creates a copy of the Bytebuf.
     *
     * @return the copied Bytebuf instance
     */
    Bytebuf copy();

    /**
     * Retains the Bytebuf, increasing its reference count.
     */
    void retain();

    /**
     * Releases the Bytebuf.
     *
     * @return true if the Bytebuf was successfully released, false otherwise
     */
    boolean release();
}
