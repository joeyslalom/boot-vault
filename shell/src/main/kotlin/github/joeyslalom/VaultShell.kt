package github.joeyslalom

import github.joeyslalom.vault.TransitService
import org.springframework.beans.factory.annotation.Value
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.vault.core.VaultTemplate

@ShellComponent
class VaultShell(private val vaultTemplate: VaultTemplate, private val transitService: TransitService) {

    @Value("\${vault.secret.path}")
    lateinit var secretPath: String

    @Value("\${vault.role.path}")
    lateinit var rolePath: String

    @ShellMethod("list secrets")
    fun listSecrets(): List<String> = vaultTemplate.list(secretPath)!!

    @ShellMethod("list AWS roles")
    fun listRoles(): List<String> = vaultTemplate.list(rolePath)!!

    @ShellMethod("get AWS credentials as environment variables for console")
    fun getRole(role: String): List<String> {
        val data: Map<String, Any> = vaultTemplate.read(String.format("aws/sts/%s", role)).data!!
        return listOf(
                "export AWS_ACCESS_KEY_ID=${data["access_key"]}",
                "export AWS_SECRET_ACCESS_KEY=${data["secret_key"]}",
                "export AWS_SESSION_TOKEN=${data["security_token"]}"
        )
    }

    @ShellMethod("encrypt plaintext")
    fun encrypt(plaintext: String): String {
        return transitService.encrypt(plaintext)
    }

    @ShellMethod("decrypt ciphertext")
    fun decrypt(ciphertext: String): String {
        return transitService.decrypt(ciphertext)
    }

    @ShellMethod("list transit keys")
    fun listTransitKeys(): List<String> {
        return transitService.keys()
    }
}

