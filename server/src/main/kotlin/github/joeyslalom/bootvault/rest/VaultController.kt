package github.joeyslalom.bootvault.rest

import github.joeyslalom.bootvault.SnsClient
import github.joeyslalom.bootvault.rest.api.AwsApi
import github.joeyslalom.bootvault.rest.api.SecretApi
import github.joeyslalom.bootvault.rest.api.TransitApi
import github.joeyslalom.bootvault.rest.model.PublishResponse
import github.joeyslalom.vault.TransitService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.vault.core.VaultOperations
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody


@Controller
class AwsController(private val snsClient: SnsClient) : AwsApi {
    override fun postMessageToSns(@PathVariable topicArn: String, @RequestBody body: String): ResponseEntity<PublishResponse> {
        val messageId = snsClient.publish(topicArn, body)
        return ResponseEntity.ok(PublishResponse().messageId(messageId))
    }
}

@Controller
class TransitController(private val transitService: TransitService) : TransitApi {

    override fun getListKeys(): ResponseEntity<List<String>> = ResponseEntity.ok(transitService.keys())

    override fun putEncrypt(@RequestBody plainText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitService.encrypt(plainText))

    override fun putDecrypt(@RequestBody cipherText: String): ResponseEntity<String> =
            ResponseEntity.ok(transitService.decrypt(cipherText))
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