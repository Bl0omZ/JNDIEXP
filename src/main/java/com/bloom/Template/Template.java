package com.bloom.Template;

public interface Template {
    void generate();
    byte[] getBytes();
    void cache();
    String getClassName();
}
