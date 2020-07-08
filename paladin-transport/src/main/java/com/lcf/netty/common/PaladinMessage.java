package com.lcf.netty.common;

import com.lcf.constants.RpcConstans;

/**
 * @author k.arthur
 * @date 2020.7.8
 */
public class PaladinMessage {

    /**
     * 魔数
     */
    private int header = RpcConstans.MESSAGE_HEAD;

    /**
     * 消息长度
     */
    private int contentLength;

    /**
     * 消息类型
     */
    private int type;

    /**
     * 消息内容
     */
    private byte[] content;

    public PaladinMessage(int contentLength, int type, byte[] content) {
        this.contentLength = contentLength+4;
        this.type=type;
        this.content = content;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
