package github.joeyslalom.vault

import org.springframework.vault.core.VaultTransitOperations
import org.springframework.vault.support.VaultTransitContext

class TransitService(private val transitOps: VaultTransitOperations,
                     private val encryptionKey: String,
                     encryptionContext: String) {

    private val transitContext: VaultTransitContext = VaultTransitContext.fromContext(encryptionContext.toByteArray())

    fun decrypt(cipherText: String) = String(transitOps.decrypt(encryptionKey, cipherText, transitContext))

    fun encrypt(plainText: String): String = transitOps.encrypt(encryptionKey, plainText.toByteArray(), transitContext)

    fun keys(): List<String> = transitOps.keys
}
