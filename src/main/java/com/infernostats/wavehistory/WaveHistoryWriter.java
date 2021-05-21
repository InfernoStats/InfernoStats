package com.infernostats.wavehistory;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class WaveHistoryWriter {
    public void toFile(String username, String filename, String contents)
    {
        File dir = new File(RUNELITE_DIR, "inferno-stats/" + username);
        dir.mkdirs();

        try (FileWriter fw = new FileWriter(new File(dir, filename)))
        {
            fw.write(contents);
        }
        catch (IOException ex)
        {
            log.debug("Error writing file: {}", ex.getMessage());
        }
    }
}
