package ru.dataquire.queryexecutor.configuration

import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyFactory
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

@Configuration
class DecryptConfiguration {
    private val logger = KotlinLogging.logger { }

    @Value("\${secret.rsa.private}")
    private val privateKey: String? = null

    @Bean
    fun decryptCipher(): Cipher = Cipher.getInstance("RSA").apply {
        init(Cipher.DECRYPT_MODE, getPrivateKey())
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