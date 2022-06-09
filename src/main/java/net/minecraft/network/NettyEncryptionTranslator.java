package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptionTranslator
{
    private final Cipher cipher;
    private byte[] field_150505_b = new byte[0];
    private byte[] field_150506_c = new byte[0];

    protected NettyEncryptionTranslator(Cipher cipherIn)
    {
        cipher = cipherIn;
    }

    private byte[] func_150502_a(ByteBuf p_150502_1_)
    {
        int i = p_150502_1_.readableBytes();

        if (field_150505_b.length < i)
        {
            field_150505_b = new byte[i];
        }

        p_150502_1_.readBytes(field_150505_b, 0, i);
        return field_150505_b;
    }

    protected ByteBuf decipher(ChannelHandlerContext ctx, ByteBuf buffer) throws ShortBufferException
    {
        int i = buffer.readableBytes();
        byte[] abyte = func_150502_a(buffer);
        ByteBuf bytebuf = ctx.alloc().heapBuffer(cipher.getOutputSize(i));
        bytebuf.writerIndex(cipher.update(abyte, 0, i, bytebuf.array(), bytebuf.arrayOffset()));
        return bytebuf;
    }

    protected void cipher(ByteBuf p_150504_1_, ByteBuf p_150504_2_) throws ShortBufferException
    {
        int i = p_150504_1_.readableBytes();
        byte[] abyte = func_150502_a(p_150504_1_);
        int j = cipher.getOutputSize(i);

        if (field_150506_c.length < j)
        {
            field_150506_c = new byte[j];
        }

        p_150504_2_.writeBytes(field_150506_c, 0, cipher.update(abyte, 0, i, field_150506_c));
    }
}
