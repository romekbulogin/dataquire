package ru.dataquire.databasemanager.configuration

import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Configuration
class DecryptConfiguration {
    private val logger = KotlinLogging.logger { }

    @Value("\${secret.rsa.public}")
    private val publicKey: String? = null

    @Value("\${secret.rsa.private}")
    private val privateKey: String? = null

    @Bean
    fun encryptCipher(): Cipher = Cipher.getInstance("RSA").apply {
        init(Cipher.ENCRYPT_MODE, getPublicKey())
    }

    @Bean
    fun decryptCipher(): Cipher = Cipher.getInstance("RSA").apply {
        init(Cipher.DECRYPT_MODE, getPrivateKey())
    }

    private fun getPublicKey(): PublicKey {
        logger.info("PUBLIC KEY: $publicKey")

        val publicKeyResult = publicKey
            ?.replace("-----BEGIN PUBLIC KEY-----", "")
            ?.replace("\n", "")
            ?.replace("-----END PUBLIC KEY-----", "")

        val encoded: ByteArray = Base64.getDecoder().decode(publicKeyResult)

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as PublicKey
    }

    private fun getPrivateKey(): RSAPrivateKey {
        logger.info("PRIVATE KEY: $privateKey")
        Security.addProvider(
            BouncyCastleProvider()
        )

        val privateKeyResult = privateKey
            ?.replace("-----BEGIN RSA PRIVATE KEY-----", "")
            ?.replace("\n", "")
            ?.replace("-----END RSA PRIVATE KEY-----", "")

        val encoded: ByteArray = Base64.getDecoder().decode(privateKeyResult)

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }
}