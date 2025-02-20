package com.jianbing.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
