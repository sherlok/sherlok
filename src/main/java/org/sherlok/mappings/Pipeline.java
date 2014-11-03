package org.sherlok.mappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Pipeline {

    private int port;

    public Pipeline() {

    }

    @JsonProperty("sherlok.port")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

  
  

    public static Pipeline load(File configFile)
            throws JsonParseException, JsonMappingException,
            FileNotFoundException, IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new FileInputStream(configFile),
                Pipeline.class);
    }
}
