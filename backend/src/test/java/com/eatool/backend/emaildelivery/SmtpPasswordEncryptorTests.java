package com.eatool.backend.emaildelivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SmtpPasswordEncryptor} (issue #24): the SMTP password
 * round-trips through AES-GCM encryption, the ciphertext is not the plaintext,
 * and two encryptions of the same plaintext differ (fresh random IV).
 */
class SmtpPasswordEncryptorTests {

    private static final String KEY =
            Base64.getEncoder().encodeToString("unit-test-aes-key-32-bytes-long!".getBytes());

    @Test
    void encryptsAndDecryptsRoundTrip() {
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);

        String ciphertext = encryptor.encrypt("s3cr3t-pass");

        assertThat(ciphertext).isNotEqualTo("s3cr3t-pass");
        assertThat(encryptor.decrypt(ciphertext)).isEqualTo("s3cr3t-pass");
    }

    @Test
    void producesDifferentCiphertextEachTime() {
        SmtpPasswordEncryptor encryptor = new SmtpPasswordEncryptor(KEY);

        String first = encryptor.encrypt("same-value");
        String second = encryptor.encrypt("same-value");

        assertThat(first).isNotEqualTo(second);
        assertThat(encryptor.decrypt(first)).isEqualTo("same-value");
        assertThat(encryptor.decrypt(second)).isEqualTo("same-value");
    }
}
