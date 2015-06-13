/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.socks.test;

import com.xx_dev.apn.socks.TextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyAESDecoder 14-6-28 12:09 (xmx) Exp $
 */
public class FakeHttpClientDecoder extends ReplayingDecoder<FakeHttpClientDecoder.STATE> {

    private byte key = 0x23;

    enum STATE {
        READ_SKIP_1,
        READ_LENGTH,
        READ_SKIP_2,
        READ_CONTENT
    }

    private int length;



    public FakeHttpClientDecoder() {
        super(STATE.READ_SKIP_1);


    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
            case READ_SKIP_1: {
                in.skipBytes(48);
                this.checkpoint(STATE.READ_LENGTH);
            }
            case READ_LENGTH: {
                byte[] buf = new byte[8];
                in.readBytes(buf);

                String s = TextUtil.fromUTF8Bytes(buf);

                length = Integer.parseInt(s, 16);
                this.checkpoint(STATE.READ_SKIP_2);
            }
            case READ_SKIP_2: {
                in.skipBytes(63);
                this.checkpoint(STATE.READ_CONTENT);
            }
            case READ_CONTENT: {
                byte[] buf = new byte[length];
                in.readBytes(buf);

                byte[] res = new byte[length];

                for (int i=0; i<length; i++) {
                    res[i] = (byte)(buf[i] ^ key);
                }

                ByteBuf outBuf = ctx.alloc().buffer();

                outBuf.writeBytes(res);

                out.add(outBuf);
            }
            default:
                throw new Error("Shouldn't reach here.");
        }


    }


}