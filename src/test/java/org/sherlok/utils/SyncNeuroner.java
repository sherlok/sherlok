/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sherlok.utils;

import static com.google.common.io.Files.readLines;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.sherlok.FileBased.parsePipeline;

import java.io.File;
import java.util.List;

import org.sherlok.FileBased;
import org.sherlok.mappings.PipelineDef;

import com.google.common.io.Files;

/**
 * Copies resources fiels and main script from Ruta workbench into Sherlok
 * 
 * @see Obo2Wordtable as well
 * @author richarde
 */
public class SyncNeuroner {

    public static void main(String[] args) throws Exception {

        String NEURONER_ROOT = "/Users/richarde/git2/neuroNER/";

        String sherlokPipelinePath = "config/pipelines/bluima/neuroner/neuroner_0.1.json";
        String rutaPipelinePath = NEURONER_ROOT
                + "script/neuroner/NeuroNER.ruta";

        // update script
        PipelineDef p = parsePipeline(Files.toString(new File(
                sherlokPipelinePath), UTF_8));
        List<String> rutaLines = readLines(new File(rutaPipelinePath), UTF_8);
        p.setScriptLines(rutaLines);
        FileBased.write(new File(sherlokPipelinePath), p); // write back

        // copy NeuroNER resources
        copyDirectory(new File(NEURONER_ROOT + "resources/bluima/neuroner"),
                new File("config/resources/bluima/neuroner"));

        System.out.println("Done :-)");
    }
}
