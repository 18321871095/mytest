//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.fr.activiti.rest.servlet;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class FilterServletOutputStream extends ServletOutputStream {
    private DataOutputStream stream;
    private WriteListener writeListener;

    public FilterServletOutputStream(OutputStream output) {
        this.stream = new DataOutputStream(output);
    }

    public void write(int b) throws IOException {
        this.stream.write(b);
    }

    public void write(byte[] b) throws IOException {
        this.stream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.stream.write(b, off, len);
    }

    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

    public boolean isReady() {
        return true;
    }
}
