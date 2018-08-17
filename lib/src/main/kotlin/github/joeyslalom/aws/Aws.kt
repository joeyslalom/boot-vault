package github.joeyslalom.aws

import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.auth.AWSSessionCredentialsProvider
import org.springframework.core.env.Environment
import java.util.concurrent.atomic.AtomicReference

data class Ec2Info(val accountId: String, val region: String)

data class StsCredentials
private constructor(private val accessKey: String,
                    private val secretKey: String,
                    private val securityToken: String) : AWSSessionCredentials {

    override fun getSessionToken(): String = securityToken

    override fun getAWSAccessKeyId(): String = accessKey

    override fun getAWSSecretKey(): String = secretKey

    companion object {
        val EMPTY = StsCredentials("", "", "")

        fun of(accessKey: String?, secretKey: String?, securityToken: String?): StsCredentials {
            if (accessKey == null || secretKey == null || securityToken == null) return EMPTY
            if (accessKey.isNotBlank() && secretKey.isNotBlank() && securityToken.isNotBlank()) {
                return StsCredentials(accessKey, secretKey, securityToken)
            }
            return EMPTY
        }

        fun of(env: Environment, prefix: String) = of(
                env.getProperty("${prefix}access_key"),
                env.getProperty("${prefix}secret_key"),
                env.getProperty("${prefix}security_token")
        )
    }
}

class SessionCredentialsProvider(private val provider: () -> StsCredentials) : AWSSessionCredentialsProvider {

    private val lastCreds = AtomicReference(StsCredentials.EMPTY)

    override fun getCredentials(): AWSSessionCredentials {
        refresh()
        return lastCreds.get()
    }

    override fun refresh() {
        val refreshed: StsCredentials = provider()
        if (refreshed != StsCredentials.EMPTY) {
            lastCreds.set(refreshed)
        }
    }
}
