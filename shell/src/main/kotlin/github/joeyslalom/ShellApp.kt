package github.joeyslalom

import github.joeyslalom.aws.AwsS3Config
import github.joeyslalom.vault.VaultGenericConfig
import github.joeyslalom.vault.VaultTransitConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@SpringBootApplication
class ShellApp

fun main(args: Array<String>) {
    SpringApplication.run(ShellApp::class.java, *args)
}

@Configuration
@Import(value = [VaultGenericConfig::class, VaultTransitConfig::class, AwsS3Config::class])
class VaultConfig
