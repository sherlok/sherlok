package org.sherlok.mappings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.sherlok.Sherlok;

public class MavenPom {

    void create(String pipelineName, String pipelineVersion, Bundle bundle) throws IOException {
        
        Model model = new Model();
        model.setGroupId("some.group.id");

        FileWriter writer = new FileWriter(new File(Sherlok.LOCAL_REPO_PATH
                + ""));
        new MavenXpp3Writer().write(writer, model);
    }

}
