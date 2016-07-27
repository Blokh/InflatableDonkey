/*
 * The MIT License
 *
 * Copyright 2016 Ahseya.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.horrorho.inflatabledonkey.chunk.engine;

import com.github.horrorho.inflatabledonkey.chunk.store.ChunkStore;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.jcip.annotations.Immutable;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 *
 * @author Ahseya
 */
@Immutable
public final class ChunkListDecrypterFactory {

    public static ChunkListDecrypterFactory defaults() {
        return DEFAULT_INSTANCE;
    }

    static BiFunction<byte[], InputStream, CipherInputStream> cipherInputStreams(Supplier<StreamBlockCipher> ciphers) {
        return (key, inputStream) -> {
            StreamBlockCipher cipher = ciphers.get();
            cipher.init(false, new KeyParameter(key));
            return new CipherInputStream(inputStream, cipher);
        };
    }

    private static final ChunkListDecrypterFactory DEFAULT_INSTANCE
            = new ChunkListDecrypterFactory(cipherInputStreams(ChunkCiphers::cipher), ChunkKeys::unwrap);

    private final BiFunction< byte[], InputStream, CipherInputStream> cipherInputStreams;
    private final BiFunction<byte[], byte[], Optional<byte[]>> keyUnwrap;

    public ChunkListDecrypterFactory(
            BiFunction<byte[], InputStream, CipherInputStream> cipherInputStreams,
            BiFunction<byte[], byte[], Optional<byte[]>> keyUnwrap) {
        this.cipherInputStreams = Objects.requireNonNull(cipherInputStreams);
        this.keyUnwrap = Objects.requireNonNull(keyUnwrap);
    }

    public ChunkListDecrypter apply(ChunkStore store, ChunksContainer container) {
        return new ChunkListDecrypter(cipherInputStreams, keyUnwrap, store, container);
    }
}
