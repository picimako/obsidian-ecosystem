package com.picimako.obsidian.plugin;

/**
 * Represents the data stored in the {@code manifest.json} file.
 *
 * @see <a href="https://docs.obsidian.md/Reference/Manifest">Manifest</a>
 */
public final class Manifest {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
