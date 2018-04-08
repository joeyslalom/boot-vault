package github.joeyslalom.bootvault

import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.auth.AWSSessionCredentialsProvider
import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.vault.annotation.VaultPropertySource


private const val STS_PREFIX_SNS = "sts.sns."


@Configuration
@VaultPropertySource(
        value = ["aws/sts/use1-ucp-dev-snsaccess"],
        renewal = VaultPropertySource.Renewal.ROTATE,
        propertyNamePrefix = STS_PREFIX_SNS
)
class SnsVaultProps(private val env: Environment) : CredentialsProvider {
    override fun credentials() = Credentials.of(STS_PREFIX_SNS, env)
}

interface CredentialsProvider : AWSSessionCredentialsProvider {
    fun credentials(): Credentials

    override fun getCredentials(): AWSSessionCredentials = credentials()

    override fun refresh() {
    }
}

class Credentials(
        val accessKey: String,
        val secretKey: String,
        val securityToken: String) : AWSSessionCredentials {

    override fun getSessionToken(): String = securityToken

    override fun getAWSAccessKeyId(): String = accessKey

    override fun getAWSSecretKey(): String = secretKey

    companion object {
        fun of(prefix: String, env: Environment) = Credentials(
                env.getProperty("${prefix}access_key", ""),
                env.getProperty("${prefix}secret_key", ""),
                env.getProperty("${prefix}security_token", "")
        )
    }
}

private const val AWS_REGION = "us-east-1"

@Configuration
class Config {
    @Bean
    fun amazonSns(provider: SnsVaultProps): AmazonSNSAsync {
        return AmazonSNSAsyncClientBuilder.standard()
                .withRegion(AWS_REGION)
                .withCredentials(provider)
                .build()
    }
}

@Component
class SnsClient(private val amazonSnsAsync: AmazonSNSAsync) {
    fun publish(topicArn: String, message: String): String = amazonSnsAsync.publish(topicArn, message).messageId
}
