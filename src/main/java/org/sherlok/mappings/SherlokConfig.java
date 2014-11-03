package org.sherlok.mappings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.sherlok.Sherlok;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SherlokConfig {

    @JsonProperty("sherlok.port")
    private int port;

    public static SherlokConfig load() throws JsonParseException,
            JsonMappingException, FileNotFoundException, IOException {
        // TODO allow overload on cli
        return load(new File(Sherlok.CONFIG_FILE_PATH));
    }

    public static SherlokConfig load(File configFile)
            throws JsonParseException, JsonMappingException,
            FileNotFoundException, IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new FileInputStream(configFile),
                SherlokConfig.class);
    }

    public int getPort() {
        return port;
    }
}
