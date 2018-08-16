package github.joeyslalom.bootvault

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import github.joeyslalom.aws.SessionCredentialsProvider
import github.joeyslalom.aws.StsCredentials
import github.joeyslalom.vault.VaultGenericConfig
import github.joeyslalom.vault.VaultTransitConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.vault.annotation.VaultPropertySource


private const val STS_PREFIX_SNS = "sts.sns."
private const val AWS_REGION = "us-east-1"


@Configuration
@Import(value = [VaultGenericConfig::class, VaultTransitConfig::class])
@VaultPropertySource(
        value = ["aws/sts/use1-ucp-dev-snsaccess"],
        renewal = VaultPropertySource.Renewal.ROTATE,
        propertyNamePrefix = STS_PREFIX_SNS
)
class SnsPropertySource(private val env: Environment) {
    @Bean
    fun snsCredentialsProvider() = SessionCredentialsProvider {
        StsCredentials.of(env, STS_PREFIX_SNS)
    }
}

@Configuration
class SnsConfig(private val snsCredentialsProvider: SessionCredentialsProvider) {
    @Bean
    fun amazonSns(): AmazonSNSAsync = AmazonSNSAsyncClientBuilder.standard()
            .withRegion(AWS_REGION)
            .withCredentials(snsCredentialsProvider)
            .build()
}

@Component
class SnsClient(private val amazonSnsAsync: AmazonSNSAsync) {
    fun publish(topicArn: String, message: String): String = amazonSnsAsync.publish(topicArn, message).messageId
}
