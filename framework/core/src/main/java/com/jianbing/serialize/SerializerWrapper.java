package com.jianbing.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
