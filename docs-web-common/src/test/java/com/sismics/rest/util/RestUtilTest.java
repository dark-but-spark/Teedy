package com.sismics.rest.util;

import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.rest.exception.ServerException;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestUtilTest {
    @Test
    void shouldBuildJsonUsingStoredSize() {
        File file = buildFile("known-size", 123L);

        JsonObject json = RestUtil.fileToJsonObjectBuilder(file).build();

        assertEquals("known-size", json.getString("id"));
        assertFalse(json.getBoolean("processing"));
        assertEquals("contract.pdf", json.getString("name"));
        assertEquals(2, json.getInt("version"));
        assertEquals("application/pdf", json.getString("mimetype"));
        assertEquals("doc-1", json.getString("document_id"));
        assertEquals(1700000000000L, json.getJsonNumber("create_date").longValue());
        assertEquals(123L, json.getJsonNumber("size").longValue());
    }

    @Test
    void shouldBuildJsonUsingFileSystemSizeWhenDatabaseSizeUnknown() throws Exception {
        String fileId = UUID.randomUUID().toString();
        Path storedFile = DirectoryUtil.getStorageDirectory().resolve(fileId);
        Files.write(storedFile, "hello".getBytes(StandardCharsets.UTF_8));

        try {
            File file = buildFile(fileId, File.UNKNOWN_SIZE);

            JsonObject json = RestUtil.fileToJsonObjectBuilder(file).build();

            assertEquals(5L, json.getJsonNumber("size").longValue());
        } finally {
            Files.deleteIfExists(storedFile);
        }
    }

    @Test
    void shouldWrapIoErrorsWhenUnknownSizeFileIsMissing() {
        File file = buildFile(UUID.randomUUID().toString(), File.UNKNOWN_SIZE);

        assertThrows(ServerException.class, () -> RestUtil.fileToJsonObjectBuilder(file).build());
    }

    private File buildFile(String id, long size) {
        File file = new File();
        file.setId(id);
        file.setName("contract.pdf");
        file.setVersion(2);
        file.setMimeType("application/pdf");
        file.setDocumentId("doc-1");
        file.setCreateDate(new Date(1700000000000L));
        file.setSize(size);
        return file;
    }
}
