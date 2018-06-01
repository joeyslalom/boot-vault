package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.SnsClient
import github.joeyslalom.bootvault.SnsVaultProps
import github.joeyslalom.bootvault.rest.api.AwsApi
import github.joeyslalom.bootvault.rest.api.SecretApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import github.joeyslalom.bootvault.rest.model.PublishResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.VaultTransitContext
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader


@Controller
class AwsController(private val snsVaultProps: SnsVaultProps, private val snsClient: SnsClient) : AwsApi {
    private val log = LoggerFactory.getLogger(AwsController::class.java)

    override fun getSnsCreds(): ResponseEntity<Void> {
        val credentials = snsVaultProps.credentials()
        log.info("""copy and paste SNS creds below
export AWS_ACCESS_KEY_ID=${credentials.accessKey}
export AWS_SECRET_ACCESS_KEY=${credentials.secretKey}
export AWS_SESSION_TOKEN=${credentials.securityToken}
""")
        return ResponseEntity.noContent().build()
    }

    override fun postMessageToSns(@PathVariable topicArn: String, @RequestBody body: String): ResponseEntity<PublishResponse> {
        val messageId = snsClient.publish(topicArn, body)
        return ResponseEntity.ok(PublishResponse().messageId(messageId))
    }
}

@Controller
class TransitController(private val vaultOps: VaultOperations) : TransitApi {
    private val transitOps = vaultOps.opsForTransit()
    private val log = LoggerFactory.getLogger(TransitController::class.java)

    override fun getListKeys(): ResponseEntity<List<String>> = ResponseEntity.ok(transitOps.keys)

    override fun putEncrypt(@PathVariable key: String, @RequestBody plainText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitOps.encrypt(key, plainText))

    override fun putDecrypt(@PathVariable key: String, @RequestBody cipherText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitOps.decrypt(key, cipherText))

    override fun putDecryptConvergent(@PathVariable key: String,
                                      @RequestHeader context: String,
                                      @RequestBody cipherText: String): ResponseEntity<String> {
        val transitContext = VaultTransitContext.fromContext(context.toByteArray())
        return ResponseEntity.ok(String(transitOps.decrypt(key, cipherText, transitContext)))
    }

    override fun putEncryptConvergent(@PathVariable key: String,
                                      @RequestHeader context: String,
                                      @RequestBody plainText: String): ResponseEntity<String> {
        val transitContext = VaultTransitContext.fromContext(context.toByteArray())
        return ResponseEntity.ok(transitOps.encrypt(key, plainText.toByteArray(), transitContext))
    }
}

@Controller
class SecretController(private val vaultOps: VaultOperations) : SecretApi {

    override fun getSecretListGroup(@PathVariable group: String): ResponseEntity<List<String>> {
        val list: List<String> = vaultOps.list("secret/$group")!!.filterNotNull()
        return ResponseEntity.ok(list)
    }

    override fun getSecretName(@PathVariable group: String, @PathVariable name: String): ResponseEntity<Any> {
        val data: Map<String, String> = vaultOps.read("secret/$group/$name")!!.data!!.map { (k, v) -> k to v.toString() }.toMap()
        return ResponseEntity.ok(data)
    }
}