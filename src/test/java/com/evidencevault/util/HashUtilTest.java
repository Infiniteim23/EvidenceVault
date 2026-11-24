package com.evidencevault.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {
    @Test
    void sha256StringMatchesKnownVector(){
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashUtil.sha256("abc"));
    }

    @Test
    void sha256IsDeterministic(){
        assertEquals(HashUtil.sha256("EvidenceVault"), HashUtil.sha256("EvidenceVault"));
        assertNotEquals(HashUtil.sha256("EvidenceVault"), HashUtil.sha256("EvidenceVault!"));
    }

    @Test
    void sha256InputStreamMatchesStringHash() throws Exception {
        String value = "forensic-evidence";
        assertEquals(HashUtil.sha256(value), HashUtil.sha256(new java.io.ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8))));
    }
}
