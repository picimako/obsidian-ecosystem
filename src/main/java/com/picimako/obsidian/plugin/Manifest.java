package com.picimako.obsidian.plugin;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the data stored in the {@code manifest.json} file.
 *
 * @see <a href="https://docs.obsidian.md/Reference/Manifest">Manifest</a>
 */
@Getter
@Setter
public final class Manifest {
    private String id;
}
