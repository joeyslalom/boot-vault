package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.SnsClient
import github.joeyslalom.bootvault.SnsVaultProps
import github.joeyslalom.bootvault.rest.api.AwsApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import github.joeyslalom.bootvault.rest.model.PublishResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.vault.core.VaultOperations
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody


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
}