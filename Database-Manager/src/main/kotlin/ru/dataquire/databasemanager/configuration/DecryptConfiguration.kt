package ru.dataquire.databasemanager.configuration

import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@Configuration
class DecryptConfiguration {
    private val logger = KotlinLogging.logger { }

    @Value("\${secret.rsa.public}")
    private val publicKeyPath: String? = null

    @Value("\${secret.rsa.private}")
    private val privateKeyPath: String? = null

    @Bean
    fun encryptCipher(): Cipher = Cipher.getInstance("RSA").apply {
        init(Cipher.ENCRYPT_MODE, getPublicKey())
    }

    @Bean
    fun decryptCipher(): Cipher = Cipher.getInstance("RSA").apply {
        init(Cipher.DECRYPT_MODE, getPrivateKey())
    }

    private fun getPublicKey(): PublicKey {
        logger.info("PUBLIC KEY: $publicKeyPath")

        val keyBytes = Files.readAllBytes(
            Paths.get(publicKeyPath.toString())
        )
        val publicKey = String(keyBytes)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("-----END PUBLIC KEY-----", "")
        val encoded: ByteArray = Base64.getDecoder().decode(publicKey)


        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as PublicKey
    }

    private fun getPrivateKey(): RSAPrivateKey {
        logger.info("PRIVATE KEY: $privateKeyPath")
        Security.addProvider(
            BouncyCastleProvider()
        )

        val keyBytes = Files.readAllBytes(
            Paths.get(privateKeyPath.toString())
        )
        val privateKey = String(keyBytes)
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("-----END RSA PRIVATE KEY-----", "")

        val encoded: ByteArray = Base64.getDecoder().decode(privateKey)

        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }
}