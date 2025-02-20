package com.jianbing.compress;

import com.jianbing.compress.impl.GzipCompressor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CompressorFactory {

    public static final Map<String, CompressorWrapper> COMPRESSOR_CACHE_BY_TYPE = new ConcurrentHashMap<>(8);
    public static final Map<Byte, CompressorWrapper> COMPRESSOR_CACHE_BY_CODE = new ConcurrentHashMap<>(8);


    static{
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE_BY_TYPE.put("gzip", gzip);
        COMPRESSOR_CACHE_BY_CODE.put((byte) 1, gzip);
    }


    public static CompressorWrapper getCompressorWrapper(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_BY_TYPE.get(compressorType);
        if(compressorWrapper == null){
            return COMPRESSOR_CACHE_BY_TYPE.get("gzip");
        }
        return compressorWrapper;
    }
    public static CompressorWrapper getSerializerWrapper(byte compressorCode) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_BY_CODE.get(compressorCode);
        if(compressorWrapper == null){
            return COMPRESSOR_CACHE_BY_TYPE.get("gzip");
        }
        return compressorWrapper;
    }
}
