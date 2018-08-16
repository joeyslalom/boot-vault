package github.joeyslalom.aws

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import github.joeyslalom.vault.STS_PREFIX_S3
import github.joeyslalom.vault.rotatingPropertySource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.vault.core.lease.SecretLeaseContainer
import javax.annotation.PostConstruct

@Configuration
class AwsConfig {
    @Value("\${aws.account.id}")
    lateinit var accountId: String
    @Value("\${aws.region}")
    lateinit var region: String

    @Bean
    fun awsInfo() = Ec2Info(accountId, region)
}

@Configuration
class AwsS3Config {

    @Autowired
    lateinit var env: ConfigurableEnvironment

    @Autowired
    lateinit var secretLeaseContainer: SecretLeaseContainer

    @Value("\${vault.stspath.s3}")
    lateinit var stsPath: String

    /**
     * Configure S3 Client
     */
    @Bean
    fun amazonS3(awsInfo: Ec2Info, @Qualifier("s3") provider: SessionCredentialsProvider): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
                .withRegion(awsInfo.region)
                .withCredentials(provider)
                .build()
    }

    /**
     * Connect Spring Environment to the AWSCredentialsProvider
     */
    @Bean
    @Qualifier("s3")
    fun sessionCredentialsProviderS3(env: Environment) = SessionCredentialsProvider {
        StsCredentials.of(env, STS_PREFIX_S3)
    }

    /**
     * Add the LeaseAwareVaultPropertySources to the PropertySource list
     */
    @PostConstruct
    fun init() {
        env.propertySources.addFirst(rotatingPropertySource(stsPath, secretLeaseContainer, STS_PREFIX_S3))
    }

    companion object {

        @Bean
        fun placeHolderConfigurer(): PropertySourcesPlaceholderConfigurer {
            return PropertySourcesPlaceholderConfigurer()
        }
    }
}